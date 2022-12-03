package com.sunveee.framework.rabbitmq.rpc.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 消费者(rpc服务端)配置项 RabbitMQRPCConsumerProperties.java
 *
 * @author SunVeee
 * @version 2020-12-24 16:05:52
 */
@Data
@ConfigurationProperties(RPCConsumerProperties.RPC_CONSUMER_PREFIX)
public class RPCConsumerProperties {

    public static final String RPC_CONSUMER_PREFIX = "rabbit-mq-starter.rpc.consumer";

}
