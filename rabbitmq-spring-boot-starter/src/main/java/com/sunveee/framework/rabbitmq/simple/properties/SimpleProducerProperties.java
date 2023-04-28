package com.sunveee.framework.rabbitmq.simple.properties;

import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * 生产者配置项
 *
 * @author SunVeee
 * @version 2021-01-11 14:49:12
 */
@Data
@Validated
@ConfigurationProperties(SimpleProducerProperties.SIMPLE_PRODUCER_PREFIX)
public class SimpleProducerProperties {

    public static final String SIMPLE_PRODUCER_PREFIX = "rabbit-mq-starter.simple.producer";

    public static final String SIMPLE_PRODUCER_ENABLED = "rabbit-mq-starter.simple.producer.enabled";

    private boolean enabled = true;

    /**
     * 发送超时（毫秒）
     */
    @Min(1)
    private long initialSendTimeout = 3000;

    /**
     * 最大发送超时（毫秒）
     */
    @Min(1)
    private long globalMaxSendTimeout = 3000;

    /**
     * 重试次数
     */
    private int retries = 0;

    /**
     * 重试间隔（秒）<br>
     * 当重试间隔数少于重试次数时，后续重试间隔取已配置的最后一次重试间隔
     */
    private String retryInterval = "1";

    /**
     * 消息return监听<br>
     * 启用时，将尝试捕获消息return callback
     */
    private boolean returnCallbackListenerEnabled = false;

}
