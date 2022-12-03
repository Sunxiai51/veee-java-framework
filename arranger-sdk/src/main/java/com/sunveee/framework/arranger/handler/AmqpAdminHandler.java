package com.sunveee.framework.arranger.handler;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunveee.framework.arranger.common.constants.TaskConstants;

@Component
public class AmqpAdminHandler {

    private static final ConcurrentHashMap<String/* taskType */, String/* exchangeName */> TASK_STEP_EXCHANGE_CACHE = new ConcurrentHashMap<String, String>();

    @Autowired
    private AmqpAdmin amqpAdmin;

    public String exchangeName(String taskType) {
        if (TASK_STEP_EXCHANGE_CACHE.contains(taskType)) {
            return TASK_STEP_EXCHANGE_CACHE.get(taskType);
        }
        final String exchangeName = taskType + TaskConstants.TASK_STEP_EXCHANGE_SUFFIX;
        amqpAdmin.declareExchange(new TopicExchange(exchangeName, true, false));
        TASK_STEP_EXCHANGE_CACHE.put(taskType, exchangeName);
        return exchangeName;
    }

}
