package com.sunveee.framework.rabbitmq.rpc.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 生产者(rpc客户端)配置项 RabbitMQRPCProducerProperties.java
 *
 * @author SunVeee
 * @version 2020-12-24 16:05:32
 */
@Data
@ConfigurationProperties(RPCProducerProperties.RPC_PRODUCER_PREFIX)
public class RPCProducerProperties {

    public static final String RPC_PRODUCER_PREFIX = "rabbit-mq-starter.rpc.producer";

    /**
     * 回复超时毫秒数
     */
    private long replyTimeout = 6000;

    /**
     * 调用日志开启
     */
    private boolean invokeLogEnabled = false;

}
