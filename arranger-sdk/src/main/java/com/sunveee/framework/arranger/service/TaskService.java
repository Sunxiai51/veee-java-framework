package com.sunveee.framework.arranger.service;

import com.sunveee.framework.arranger.api.vo.*;
import com.sunveee.framework.arranger.common.constants.TaskConstants;
import com.sunveee.framework.arranger.common.enums.TaskExecStatus;
import com.sunveee.framework.arranger.common.enums.TaskStepExecStatus;
import com.sunveee.framework.arranger.common.enums.TryNextStepResult;
import com.sunveee.framework.arranger.dao.entity.Task;
import com.sunveee.framework.arranger.dao.entity.TaskStep;
import com.sunveee.framework.arranger.handler.AmqpAdminHandler;
import com.sunveee.framework.arranger.repository.TaskRepository;
import com.sunveee.framework.arranger.repository.TaskStepRepository;
import com.sunveee.framework.arranger.util.AssertUtil;
import com.sunveee.framework.rabbitmq.simple.producer.SimpleProducerSendConfig;
import com.sunveee.framework.rabbitmq.simple.producer.SimpleRabbitTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStepRepository taskStepRepository;
    @Autowired
    private SimpleRabbitTemplate simpleRabbitTemplate;
    @Autowired
    private AmqpAdminHandler amqpHandler;

    /**
     * 初始化任务
     *
     * @param createTaskInput
     */
    @Transactional(rollbackFor = Exception.class)
    public String initTask(CreateTaskInput createTaskInput) {
        Task toSaveTask = buildInitTask(createTaskInput.getTask());
        List<TaskStep> toSaveTaskSteps = buildInitTaskSteps(toSaveTask, createTaskInput.getTaskSteps());

        // 执行入库操作
        Task savedTask = taskRepository.save(toSaveTask);
        List<TaskStep> savedTaskSteps = taskStepRepository.saveAll(toSaveTaskSteps);

        log.info("Init task[{}] and steps[{}].", savedTask, savedTaskSteps);
        return savedTask.getTaskId();
    }

    /**
     * 启动任务
     *
     * @param taskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void launchTask(String taskId) {
        Task task = taskRepository.selectByTaskId(taskId);
        List<TaskStep> firstSteps = taskStepRepository.selectByTaskIdAndSeqNo(taskId, 1);

        LocalDateTime launchTime = LocalDateTime.now();

        int updateTaskCount = taskRepository.launchTask(taskId, launchTime, TaskExecStatus.INIT, TaskExecStatus.PROCESS);
        AssertUtil.assertTrue(1 == updateTaskCount, "启动任务失败:任务更新失败");

        int updateTaskStepCount = taskStepRepository.launchStep(taskId, 1, TaskStepExecStatus.INIT, TaskStepExecStatus.PREPARED);
        AssertUtil.assertTrue(firstSteps.size() == updateTaskStepCount, "启动任务失败:任务步骤更新失败");

        final String exchangeName = amqpHandler.exchangeName(task.getType());
        for (TaskStep step : firstSteps) {
            simpleRabbitTemplate.simpleSend(exchangeName, TaskConstants.TASK_STEP_ROUTINGKEY_PREPARED, step.getStepId());
        }

    }

    /**
     * 开始执行步骤
     *
     * @param stepId
     */
    @Transactional(rollbackFor = Exception.class)
    public void startStepExecution(String stepId) {
        LocalDateTime startTime = LocalDateTime.now();
        int updateTaskStepCount = taskStepRepository.startStepExecution(stepId, startTime, TaskStepExecStatus.PREPARED, TaskStepExecStatus.PROCESS);
        AssertUtil.assertTrue(1 == updateTaskStepCount, "开始执行步骤失败:任务步骤更新失败");

        TaskStep step = taskStepRepository.selectByStepId(stepId);
        Task task = taskRepository.selectByTaskId(step.getTaskId());

        AssertUtil.assertTrue(task.getExecStatus() == TaskExecStatus.PROCESS, "开始执行任务步骤失败:任务状态异常");

        final String exchangeName = amqpHandler.exchangeName(task.getType());
        simpleRabbitTemplate.simpleSend(exchangeName, TaskConstants.TASK_STEP_ROUTINGKEY_PROCESS, step.getStepId(), SimpleProducerSendConfig.builder().returnCallbackListenerEnabled(false).build());
    }

    /**
     * 结束执行步骤
     *
     * @param stepId
     * @param success
     * @param execMessage
     */
    @Transactional(rollbackFor = Exception.class)
    public void endStepExecution(String stepId, boolean success, String execMessage) {
        LocalDateTime endTime = LocalDateTime.now();
        if (null != execMessage && execMessage.length() > 1024) {
            log.info("TaskStep execMessage over max-length(1024): {}.", execMessage);
            execMessage = execMessage.substring(0, 1024);
        }
        int updateTaskStepCount = taskStepRepository.endStepExecution(stepId, endTime, TaskStepExecStatus.PROCESS, success ? TaskStepExecStatus.SUCCESS : TaskStepExecStatus.FAILED, execMessage);
        AssertUtil.assertTrue(1 == updateTaskStepCount, "结束执行步骤失败:任务步骤更新失败");

        TaskStep step = taskStepRepository.selectByStepId(stepId);
        Task task = taskRepository.selectByTaskId(step.getTaskId());

        final String exchangeName = amqpHandler.exchangeName(task.getType());
        simpleRabbitTemplate.simpleSend(exchangeName, success ? TaskConstants.TASK_STEP_ROUTINGKEY_SUCCESS : TaskConstants.TASK_STEP_ROUTINGKEY_FAILED, step.getStepId(), SimpleProducerSendConfig.builder().returnCallbackListenerEnabled(false).build());
    }

    /**
     * 尝试执行下一步骤
     *
     * @param stepId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public TryNextStepResult tryNextStep(String stepId) {
        TaskStep step = taskStepRepository.selectByStepId(stepId);

        // 查询当前步骤相同序号的所有步骤
        List<TaskStep> curSteps = taskStepRepository.selectByTaskIdAndSeqNo(step.getTaskId(), step.getSeqNo());
        Optional<TaskStep> unsuccessStep = curSteps.stream().filter(x -> x.getExecStatus() != TaskStepExecStatus.SUCCESS).findAny();
        // 存在未成功的步骤
        if (unsuccessStep.isPresent()) {
            return TryNextStepResult.CUR_SEQ_NOT_ALL_SUCCESS;
        }
        // 当前全部步骤已成功
        else {
            // 查询下一步骤
            List<TaskStep> nextSteps = taskStepRepository.selectByTaskIdAndSeqNo(step.getTaskId(), step.getSeqNo() + 1);
            // 不存在下一步骤，当前步骤为最后一个步骤
            if (CollectionUtils.isEmpty(nextSteps)) {
                // 更新任务结果为完成
                taskRepository.finishTask(step.getTaskId(), LocalDateTime.now(), TaskExecStatus.PROCESS, TaskExecStatus.FINISH);
                // 发送任务完成事件
                final String exchangeName = amqpHandler.exchangeName(taskRepository.selectByTaskId(step.getTaskId()).getType());
                simpleRabbitTemplate.simpleSend(exchangeName, TaskConstants.TASK_STEP_ROUTINGKEY_ALLSUCCESS, step.getStepId(), SimpleProducerSendConfig.builder().returnCallbackListenerEnabled(false).build());
                return TryNextStepResult.CUR_SEQ_IS_FINAL_SEQ;
            }
            // 存在下一步骤
            else {
                Optional<TaskStep> notInitStep = nextSteps.stream().filter(x -> x.getExecStatus() != TaskStepExecStatus.INIT).findAny();
                if (notInitStep.isPresent()) {
                    return TryNextStepResult.NEXT_SEQ_LAUNCHED_BY_OTHER_THREAD;
                } else {
                    Task task = taskRepository.selectByTaskId(step.getTaskId());

                    int updateTaskStepCount = taskStepRepository.launchStep(step.getTaskId(), step.getSeqNo() + 1, TaskStepExecStatus.INIT, TaskStepExecStatus.PREPARED);
                    if (0 == updateTaskStepCount) {
                        return TryNextStepResult.NEXT_SEQ_LAUNCHED_BY_OTHER_THREAD;
                    }
                    AssertUtil.assertTrue(nextSteps.size() == updateTaskStepCount, "尝试执行下一步骤失败:任务步骤更新失败");

                    final String exchangeName = amqpHandler.exchangeName(task.getType());
                    for (TaskStep nextStep : nextSteps) {
                        simpleRabbitTemplate.simpleSend(exchangeName, TaskConstants.TASK_STEP_ROUTINGKEY_PREPARED, nextStep.getStepId());
                    }

                    return TryNextStepResult.NEXT_SEQ_LAUNCHED;
                }
            }
        }

    }

    /**
     * 检查步骤执行状态
     * <p>
     * 当步骤执行状态匹配target时通过检查，不匹配target但命中waitList时等待一段时间后重新检查，重新检查时匹配target仍视为通过检查
     *
     * @param stepId         步骤id
     * @param target         目标状态
     * @param waitList       等待状态组
     * @param waitMillsArray 等待时间数组(毫秒)
     */
    public void checkStepExecStatusWithWait(String stepId, TaskStepExecStatus target, List<TaskStepExecStatus> waitList, long[] waitMillsArray) {
        TaskStep step = taskStepRepository.selectByStepId(stepId);

        // 匹配target状态，check通过
        if (null != step && step.getExecStatus() == target) {
            return;
        }

        for (long waitMills : waitMillsArray) {
            if (!CollectionUtils.isEmpty(waitList) && waitList.contains(step.getExecStatus())) {
                // 匹配wait状态，等待一段时间后重新检查
                try {
                    Thread.sleep(waitMills);
                } catch (InterruptedException e) {
                }
                log.debug("Check taskStep[{}] status after {}ms.", stepId, waitMills);
                step = taskStepRepository.selectByStepId(stepId);
                if (null != step && step.getExecStatus() == target) {
                    return;
                }
            } else {
                throw new RuntimeException("步骤execStatus异常");
            }
        }

        throw new RuntimeException("步骤execStatus异常");
    }

    /**
     * 检查任务执行状态
     * <p>
     * 当任务执行状态匹配target时通过检查，不匹配target但命中waitList时等待一段时间后重新检查，重新检查时匹配target仍视为通过检查
     *
     * @param taskId         任务id
     * @param target         目标状态
     * @param waitList       等待状态组
     * @param waitMillsArray 等待时间数组(毫秒)
     */
    public void checkTaskExecStatusWithWait(String taskId, TaskExecStatus target, List<TaskExecStatus> waitList, long[] waitMillsArray) {
        Task task = taskRepository.selectByTaskId(taskId);

        // 匹配target状态，check通过
        if (task.getExecStatus() == target) {
            return;
        }

        for (long waitMills : waitMillsArray) {
            if (!CollectionUtils.isEmpty(waitList) && waitList.contains(task.getExecStatus())) {
                // 匹配wait状态，等待一段时间后重新检查
                try {
                    Thread.sleep(waitMills);
                } catch (InterruptedException e) {
                }
                log.debug("Check task[{}] execStatus after {}ms.", taskId, waitMills);
                task = taskRepository.selectByTaskId(taskId);
                if (task.getExecStatus() == target) {
                    return;
                }
            } else {
                throw new RuntimeException("任务execStatus异常");
            }
        }

        throw new RuntimeException("任务execStatus异常");
    }

    /**
     * 查询任务
     *
     * @param taskId
     * @return may return {@code null}
     */
    public TaskQueryVO queryTask(String taskId) {
        TaskQueryVO result = null;
        Task task = taskRepository.selectByTaskId(taskId);
        if (null != task) {
            result = TaskQueryVO.builder().build();
            BeanUtils.copyProperties(task, result);
        }
        return result;
    }

    /**
     * 查询步骤列表
     *
     * @param taskId
     * @return may return {@code null}
     */
    public List<TaskStepQueryVO> querySteps(String taskId) {
        List<TaskStepQueryVO> result = null;
        List<TaskStep> steps = taskStepRepository.selectByTaskId(taskId);
        if (!CollectionUtils.isEmpty(steps)) {
            result = new ArrayList<>(steps.size());
            for (TaskStep step : steps) {
                TaskStepQueryVO vo = TaskStepQueryVO.builder().build();
                BeanUtils.copyProperties(step, vo);
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 查询步骤
     *
     * @param stepId
     * @return may return {@code null}
     */
    public TaskStepQueryVO queryStep(String stepId) {
        TaskStepQueryVO result = null;
        TaskStep step = taskStepRepository.selectByStepId(stepId);
        if (null != step) {
            result = TaskStepQueryVO.builder().build();
            BeanUtils.copyProperties(step, result);
        }
        return result;
    }

    /**
     * 中止任务
     *
     * @param taskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void stopTask(String taskId) {
        Task task = taskRepository.selectByTaskId(taskId);
        AssertUtil.assertNotNull(task, "中止任务异常:task不存在");
        if (task.getExecStatus() == TaskExecStatus.STOP) {
            // 幂等
            return;
        }
        AssertUtil.assertTrue(task.getExecStatus() != TaskExecStatus.FINISH, "中止任务异常:task已完成");

        int updateCount = taskRepository.updateTaskExecStatus(taskId, task.getExecStatus(), TaskExecStatus.STOP);
        AssertUtil.assertTrue(1 == updateCount, "中止任务异常:存在并发,请稍后重试");
    }

    /**
     * 暂停任务
     *
     * @param taskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void pauseTask(String taskId) {
        Task task = taskRepository.selectByTaskId(taskId);
        AssertUtil.assertNotNull(task, "暂停任务异常:task不存在");
        if (task.getExecStatus() == TaskExecStatus.PAUSE) {
            // 幂等
            return;
        }
        AssertUtil.assertTrue(task.getExecStatus() == TaskExecStatus.PROCESS, "暂停任务异常:task未处于执行中");

        int updateCount = taskRepository.updateTaskExecStatus(taskId, TaskExecStatus.PROCESS, TaskExecStatus.PAUSE);
        AssertUtil.assertTrue(1 == updateCount, "暂停任务异常:存在并发,请稍后重试");
    }

    /**
     * 继续任务
     *
     * @param taskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void proceedTask(String taskId) {
        Task task = taskRepository.selectByTaskId(taskId);
        AssertUtil.assertNotNull(task, "继续任务异常:task不存在");
        if (task.getExecStatus() == TaskExecStatus.PROCESS) {
            // 幂等
            return;
        }
        AssertUtil.assertTrue(task.getExecStatus() == TaskExecStatus.PAUSE, "继续任务异常:task未暂停");

        int updateCount = taskRepository.updateTaskExecStatus(taskId, TaskExecStatus.PAUSE, TaskExecStatus.PROCESS);
        AssertUtil.assertTrue(1 == updateCount, "继续任务异常:存在并发,请稍后重试");
    }

    /**
     * 推动步骤执行
     * <p>
     * 检索任务当前执行到的步骤序号，重新唤起当前序号的所有未完成且未在执行中的步骤
     *
     * @param taskId
     * @param recoverFailedStep
     */
    @Transactional(rollbackFor = Exception.class)
    public void promoteSteps(String taskId, boolean recoverFailedStep) {
        Task task = taskRepository.selectByTaskId(taskId);
        AssertUtil.assertTrue(task.getExecStatus() == TaskExecStatus.PROCESS, "推动任务异常:任务未处于执行状态");

        List<TaskStep> steps = taskStepRepository.selectByTaskId(taskId);
        // 寻找当前执行的步骤
        Optional<TaskStep> firstUnsuccessStep = steps.stream()
                .filter(x -> x.getExecStatus() != TaskStepExecStatus.SUCCESS)
                .sorted((x, y) -> {
                    return x.getSeqNo() - y.getSeqNo();
                })
                .findFirst();
        // 任务执行完成
        if (!firstUnsuccessStep.isPresent()) {
            return;
        }

        final String exchangeName = amqpHandler.exchangeName(task.getType());

        final int curSeqNo = firstUnsuccessStep.get().getSeqNo();
        List<TaskStep> curSteps = steps.stream().filter(x -> x.getSeqNo() == curSeqNo).collect(Collectors.toList());
        boolean existsInit = false;
        for (TaskStep step : curSteps) {
            switch (step.getExecStatus()) {
                case PROCESS:
                case SUCCESS:
                    // 略过已成功的和在执行中的step
                    continue;
                case PREPARED:
                    // 重新触发待执行步骤的事件
                    simpleRabbitTemplate.simpleSend(exchangeName, TaskConstants.TASK_STEP_ROUTINGKEY_PREPARED, step.getStepId());
                    break;
                case INIT:
                    // 如果出现INIT的step，标记，结束循环后通过taskStepRepository.launchStep触发
                    existsInit = true;
                    break;
                case FAILED:
                    if (recoverFailedStep) {
                        // 失败步骤恢复执行
                        int updateCount = taskStepRepository.resetStep(step.getStepId(), TaskStepExecStatus.FAILED, TaskStepExecStatus.PREPARED);
                        if (updateCount == 1) {
                            simpleRabbitTemplate.simpleSend(exchangeName, TaskConstants.TASK_STEP_ROUTINGKEY_PREPARED, step.getStepId());
                        }
                    }
                    break;
            }
        }
        if (existsInit) {
            // 当存在INIT状态时，当前所有步骤均应处于INIT
            AssertUtil.assertTrue(!curSteps.stream().filter(x -> x.getExecStatus() != TaskStepExecStatus.INIT).findAny().isPresent(), "数据完整性异常:task["
                    + taskId + "]存在部分初始化步骤");
            taskStepRepository.launchStep(taskId, curSeqNo, TaskStepExecStatus.INIT, TaskStepExecStatus.PREPARED);
            for (TaskStep step : curSteps) {
                simpleRabbitTemplate.simpleSend(exchangeName, TaskConstants.TASK_STEP_ROUTINGKEY_PREPARED, step.getStepId());
            }
        }

    }

    private Task buildInitTask(TaskCreateVO vo) {
        AssertUtil.assertNotNull(vo, "创建任务异常:task不能为空");
        AssertUtil.assertTrue(!StringUtils.isEmpty(vo.getTaskId()), "创建任务异常:taskId不能为空");
        AssertUtil.assertTrue(!StringUtils.isEmpty(vo.getTaskId()), "创建任务异常:type不能为空");
        Task task = new Task();
        task.setTaskId(vo.getTaskId());
        task.setType(vo.getType());
        task.setExecStatus(TaskExecStatus.INIT);
        task.setBusiData(vo.getBusiData());
        return task;
    }

    private List<TaskStep> buildInitTaskSteps(Task toSaveTask, List<TaskStepCreateVO> taskSteps) {
        AssertUtil.assertTrue(!CollectionUtils.isEmpty(taskSteps), "创建任务异常:taskSteps不能为空");

        // 校验seqNo从1开始且连续
        Set<Integer> seqNoSet = taskSteps.stream().map(TaskStepCreateVO::getSeqNo).collect(Collectors.toSet());
        List<Integer> seqNoList = seqNoSet.stream().sorted().collect(Collectors.toList());
        AssertUtil.assertTrue(seqNoList.get(0).intValue() == 1, "创建任务异常:步骤序号必须从1开始");
        AssertUtil.assertTrue(seqNoList.get(seqNoList.size() - 1).intValue() == seqNoList.size(), "创建任务异常:步骤序号必须连续");

        // 校验name不重复
        Set<String> nameSet = taskSteps.stream().map(TaskStepCreateVO::getName).collect(Collectors.toSet());
        AssertUtil.assertTrue(nameSet.size() == taskSteps.size(), "创建任务异常:存在重复的步骤name");

        List<TaskStep> result = new ArrayList<>(taskSteps.size());
        for (TaskStepCreateVO vo : taskSteps) {
            TaskStep step = new TaskStep();
            step.setStepId(UUID.randomUUID().toString().replaceAll("-", ""));
            step.setName(vo.getName());
            step.setSeqNo(vo.getSeqNo());
            step.setTaskId(toSaveTask.getTaskId());
            step.setExecStatus(TaskStepExecStatus.INIT);
            step.setBusiData(vo.getBusiData());
            step.setMaxWaitMsec(vo.getMaxWaitMsec());
            result.add(step);
        }
        return result;
    }

}
