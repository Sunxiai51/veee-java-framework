package com.sunveee.framework.rabbitmq.simple.consumer;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;

public class SimpleConsumerContainerFactory extends SimpleRabbitListenerContainerFactory {

    public SimpleConsumerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        super();
        configurer.configure(this, connectionFactory);
        this.setAcknowledgeMode(AcknowledgeMode.AUTO); // 根据执行过程是否抛出异常判断是否ack
        this.setDefaultRequeueRejected(false); // 开启消息重试时不开启requeue
    }
}
