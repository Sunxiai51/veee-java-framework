package com.sunveee.framework.rabbitmq.simple.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(SimpleProperties.SIMPLE_PREFIX)
public class SimpleProperties {

    public static final String SIMPLE_PREFIX = "rabbit-mq-starter.simple";

    public static final String SIMPLE_ENABLED = "rabbit-mq-starter.simple.enabled";

    private boolean enabled = true;

}
