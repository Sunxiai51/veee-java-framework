package com.sunveee.framework.rabbitmq.rpc.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RPCSimpleErrorHandler implements RabbitListenerErrorHandler {

    @Override
    public Object handleError(Message amqpMessage, org.springframework.messaging.Message<?> message, ListenerExecutionFailedException exception) {
        Throwable cause = exception.getCause();
        String queue = amqpMessage.getMessageProperties().getConsumerQueue();
        Object messageContent = message.getPayload();
        if (null != cause) {
            log.error("Error occured when consume queue[{}], message: {}.", queue, messageContent, cause);
            return "ERROR: " + cause.getMessage();
        } else {
            log.error("Error occured when consume queue[{}], message: {}.", queue, messageContent, exception);
            return "ERROR: " + exception.getMessage();
        }
    }

}
