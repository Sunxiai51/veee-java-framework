package com.sunveee.framework.rabbitmq.simple.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 消费端配置项
 *
 * @author SunVeee
 * @version 2021-01-15 11:18:37
 */
@Data
@ConfigurationProperties(SimpleConsumerProperties.SIMPLE_CONSUMER_PREFIX)
public class SimpleConsumerProperties {

    public static final String SIMPLE_CONSUMER_PREFIX = "rabbit-mq-starter.simple.consumer";

    public static final String SIMPLE_CONSUMER_ENABLED = "rabbit-mq-starter.simple.consumer.enabled";

    private boolean enabled = true;

}
