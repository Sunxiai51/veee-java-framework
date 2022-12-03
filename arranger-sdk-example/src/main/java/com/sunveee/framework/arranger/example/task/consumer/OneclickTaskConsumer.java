package com.sunveee.framework.arranger.example.task.consumer;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.sunveee.framework.arranger.api.ArrangerClient;
import com.sunveee.framework.arranger.api.vo.TaskQueryVO;
import com.sunveee.framework.arranger.api.vo.TaskStepQueryVO;
import com.sunveee.framework.arranger.common.constants.TaskConstants;
import com.sunveee.framework.arranger.common.enums.TaskExecStatus;
import com.sunveee.framework.arranger.example.constants.OneclickTaskConstants;
import com.sunveee.framework.rabbitmq.simple.consumer.anno.SimpleConsumerMethod;
import com.rabbitmq.client.Channel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OneclickTaskConsumer {

    @Autowired
    private ArrangerClient arrangerClient;

    /**
     * 【必选】监听prepared事件
     */
    @SimpleConsumerMethod
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "oneclick_task_step_prepared_queue", durable = "true"), exchange = @Exchange(value = OneclickTaskConstants.ONECLICK_TASK_STEP_EXCHANGE, type = ExchangeTypes.TOPIC), key = TaskConstants.TASK_STEP_ROUTINGKEY_PREPARED), containerFactory = "simpleConsumerContainerFactory")
    public void oneclickTaskStepPrepared(@Payload String content, Channel channel, Message message) {
        final String stepId = content;

        // 开始执行（获取锁）
        arrangerClient.startStepExecution(stepId);

        // 执行某一步骤
        boolean result = false;
        try {
            TaskStepQueryVO step = arrangerClient.queryStep(stepId);
            result = mockExecuteStep(step);
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            // 执行完成（释放锁）
            arrangerClient.endStepExecution(stepId, result, result ? "成功" : "哦豁");
        }

    }

    /**
     * 【非必选】监听process事件
     */
    @SimpleConsumerMethod
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "oneclick_task_step_process_queue", durable = "true"), exchange = @Exchange(value = OneclickTaskConstants.ONECLICK_TASK_STEP_EXCHANGE, type = ExchangeTypes.TOPIC), key = TaskConstants.TASK_STEP_ROUTINGKEY_PROCESS), containerFactory = "simpleConsumerContainerFactory")
    public void oneclickTaskStepProcess(@Payload String content, Channel channel, Message message) {
        final String stepId = content;
        TaskStepQueryVO step = arrangerClient.queryStep(stepId);
        log.info("任务[{}]的步骤[{}]于[{}]开始执行...", step.getTaskId(), step.getName(), step.getStartTime());
    }

    /**
     * 【非必选】监听success事件
     */
    @SimpleConsumerMethod
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "oneclick_task_step_success_queue", durable = "true"), exchange = @Exchange(value = OneclickTaskConstants.ONECLICK_TASK_STEP_EXCHANGE, type = ExchangeTypes.TOPIC), key = TaskConstants.TASK_STEP_ROUTINGKEY_SUCCESS), containerFactory = "simpleConsumerContainerFactory")
    public void oneclickTaskStepSuccess(@Payload String content, Channel channel, Message message) {
        final String stepId = content;
        TaskStepQueryVO step = arrangerClient.queryStep(stepId);
        log.info("任务[{}]的步骤[{}]于[{}]执行完成,执行结果: {}。", step.getTaskId(), step.getName(), step.getFinishTime(), step.getExecStatus());

        // 马上查询任务结果大概率无法查询到，等待事务提交
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        TaskQueryVO task = arrangerClient.queryTask(step.getTaskId());
        if (task.getExecStatus() == TaskExecStatus.FINISH) {
            log.info("任务[{}]于[{}]执行完成", task.getTaskId(), task.getFinishTime());
        }

    }

    /**
     * 【非必选】监听fail事件
     */
    @SimpleConsumerMethod
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "oneclick_task_step_fail_queue", durable = "true"), exchange = @Exchange(value = OneclickTaskConstants.ONECLICK_TASK_STEP_EXCHANGE, type = ExchangeTypes.TOPIC), key = TaskConstants.TASK_STEP_ROUTINGKEY_FAILED), containerFactory = "simpleConsumerContainerFactory")
    public void oneclickTaskStepFail(@Payload String content, Channel channel, Message message) {
        final String stepId = content;
        TaskStepQueryVO step = arrangerClient.queryStep(stepId);
        log.info("任务[{}]的步骤[{}]于[{}]执行完成,执行结果: {}。", step.getTaskId(), step.getName(), step.getFinishTime(), step.getExecStatus());

    }

    private boolean mockExecuteStep(TaskStepQueryVO step) throws InterruptedException {
        MockStepBusiData executeData = new MockStepBusiData();
        try {
            executeData = JSON.parseObject(step.getBusiData(), MockStepBusiData.class);
        } catch (Exception e) {
            // 无法解析时使用默认配置
        }

        // 执行时长模拟
        if (executeData.getSleep() > 0) {
            Thread.sleep(executeData.getSleep());
        } else if (executeData.getSleep() < 0) {
            // 随机执行0~5秒
            Thread.sleep((long) (Math.random() * 5 * 1000));
        }

        // 成功率
        return Math.random() > executeData.getFailedProb();
    }

    @Data
    public static class MockStepBusiData {
        private long sleep = -1; // 默认随机延时
        private double failedProb = 0; // 默认不失败
    }

}
