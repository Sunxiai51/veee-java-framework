package com.sunveee.framework.rabbitmq.rpc.consumer;

import com.sunveee.framework.rabbitmq.rpc.util.ResponseCodeConstant;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

import com.alibaba.fastjson.JSON;
import com.sunveee.framework.rabbitmq.rpc.util.BizResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RPCBizErrorHandler implements RabbitListenerErrorHandler {

    @Override
    public Object handleError(Message amqpMessage, org.springframework.messaging.Message<?> message, ListenerExecutionFailedException exception) {
        Throwable cause = exception.getCause();
        String queue = amqpMessage.getMessageProperties().getConsumerQueue();
        Object messageContent = message.getPayload();
        if (null != cause) {
            log.error("Error occured when consume queue[{}], message: {}.", queue, messageContent, cause);
            return JSON.toJSONString(BizResponseBuilder.build(ResponseCodeConstant.UNKNOWN_ERROR, cause.getMessage()));
        } else {
            log.error("Error occured when consume queue[{}], message: {}.", queue, messageContent, exception);
            return JSON.toJSONString(BizResponseBuilder.build(ResponseCodeConstant.UNKNOWN_ERROR, exception.getMessage()));
        }
    }

}
