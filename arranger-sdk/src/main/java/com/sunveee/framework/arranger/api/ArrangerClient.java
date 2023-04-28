package com.sunveee.framework.arranger.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunveee.framework.arranger.api.vo.*;
import com.sunveee.framework.arranger.common.enums.TaskStepExecStatus;
import com.sunveee.framework.arranger.service.TaskService;

@Component
public class ArrangerClient {

    /**
     * 默认等待时间配置
     * <p>
     * 实际开发环境中，在数据库压力较大时，进行单次500ms的等待仍出现偶发性的检查失败。<br>
     * 为了在尽可能成功的前提下有最优的等待时间，设置默认的等待时间为<code>300ms,300ms,300ms,300ms,800ms</code>
     */
    private static final long[] DEFAULT_WAIT_MILLS_ARRAY = { 300L, 300L, 300L, 300L, 800L };

    @Autowired
    private TaskService taskService;

    /**
     * 创建任务
     * 
     * @param createTaskInput
     */
    public void createTask(CreateTaskInput createTaskInput) {
        taskService.initTask(createTaskInput);
    }

    /**
     * 启动任务
     * 
     * @param taskId
     */
    public void launchTask(String taskId) {
        taskService.launchTask(taskId);
    }

    /**
     * 创建并启动任务
     * 
     * @param createTaskInput
     */
    public void createAndLaunchTask(CreateTaskInput createTaskInput) {
        taskService.initTask(createTaskInput);
        taskService.launchTask(createTaskInput.getTask().getTaskId());
    }

    /**
     * 开始执行步骤
     * 
     * @param stepId
     */
    public void startStepExecution(String stepId) {
        taskService.checkStepExecStatusWithWait(stepId, TaskStepExecStatus.PREPARED, Arrays.asList(TaskStepExecStatus.INIT, TaskStepExecStatus.FAILED), DEFAULT_WAIT_MILLS_ARRAY);
        taskService.startStepExecution(stepId);
    }

    /**
     * 结束执行步骤
     * 
     * @param stepId
     * @param success     任务步骤是否执行成功
     * @param execMessage
     */
    public void endStepExecution(String stepId, boolean success, String execMessage) {
        taskService.checkStepExecStatusWithWait(stepId, TaskStepExecStatus.PROCESS, Arrays.asList(TaskStepExecStatus.PREPARED), DEFAULT_WAIT_MILLS_ARRAY);
        taskService.endStepExecution(stepId, success, execMessage);
        if (success) {
            taskService.checkStepExecStatusWithWait(stepId, TaskStepExecStatus.SUCCESS, Arrays.asList(TaskStepExecStatus.PROCESS), DEFAULT_WAIT_MILLS_ARRAY);
            taskService.tryNextStep(stepId);
        }
    }

    /**
     * 中止任务
     * <p>
     * 中止未执行完成的任务
     * 
     * @param taskId
     */
    public void stopTask(String taskId) {
        taskService.stopTask(taskId);
    }

    /**
     * 暂停任务
     * <p>
     * 暂停执行中的任务
     * 
     * @param taskId
     */
    public void pauseTask(String taskId) {
        taskService.pauseTask(taskId);
    }

    /**
     * 继续任务
     * <p>
     * 将已暂停的任务恢复为执行中，并触发任务当前步骤的执行
     * 
     * @param taskId
     */
    public void proceedTask(String taskId) {
        taskService.proceedTask(taskId);
        taskService.promoteSteps(taskId, false);
    }

    /**
     * 推进任务
     * <p>
     * 触发执行中的任务当前步骤的执行，多用于异常场景的恢复
     * 
     * @param taskId
     * @param recoverFailedStep
     */
    public void promoteSteps(String taskId, boolean recoverFailedStep) {
        taskService.promoteSteps(taskId, recoverFailedStep);
    }

    public QueryTaskOutput queryTaskAndSteps(String taskId) {
        TaskQueryVO task = taskService.queryTask(taskId);
        List<TaskStepQueryVO> taskSteps = taskService.querySteps(taskId);
        return QueryTaskOutput.builder().task(task).taskSteps(taskSteps).build();
    }

    public TaskQueryVO queryTask(String taskId) {
        return taskService.queryTask(taskId);
    }

    public List<TaskStepQueryVO> querySteps(String taskId) {
        return taskService.querySteps(taskId);
    }

    public TaskStepQueryVO queryStep(String stepId) {
        return taskService.queryStep(stepId);
    }
}
