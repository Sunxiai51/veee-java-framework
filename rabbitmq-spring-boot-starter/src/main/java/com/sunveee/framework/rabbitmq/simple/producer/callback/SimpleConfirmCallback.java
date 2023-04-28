package com.sunveee.framework.rabbitmq.simple.producer.callback;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SimpleConfirmCallback.java
 * <p>
 * RabbitMQ Publisher Confirm机制
 * 
 * @author SunVeee
 * @version 2021-01-12 17:15:14
 */
@Slf4j
public class SimpleConfirmCallback implements ConfirmCallback {

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        // 在template中已经做了ack结果处理，这里仅使用日志进行记录
        if (ack) {
            log.debug("Message ack true, correlationData: {}.", correlationData);
        } else {
            log.debug("Message ack false, correlationData: {}, cause: {}.", correlationData, cause);
        }

    }

}
