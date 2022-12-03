package com.sunveee.framework.rabbitmq.simple.producer;

import lombok.Builder;
import lombok.Data;

/**
 * 允许生产者自定义的配置项，不为{@code null}将覆盖全局配置
 *
 * @author SunVeee
 * @version 2021-01-15 14:57:04
 */
@Data
@Builder
public class SimpleProducerSendConfig {

    private Long initialSendTimeout;

    private Integer retries;

    private String retryInterval;

    private Boolean returnCallbackListenerEnabled;

}
