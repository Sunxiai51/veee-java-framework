package com.sunveee.framework.rabbitmq.simple.producer.callback;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SimpleReturnCallback.java
 * <p>
 * 如果发送时设置了{@code mandatory}={@code true}，当消息到达exchange但无法路由到queue时，将通过该回调告知
 *
 * @author SunVeee
 * @version 2021-01-12 17:05:40
 */
@Slf4j
public class SimpleReturnCallback implements ReturnCallback {

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.debug("Message returned with replyCode[{}] and replyText[{}], exchange: {}, routingKey: {}, message: {}.", replyCode, replyText, exchange, routingKey, message);

    }

}
