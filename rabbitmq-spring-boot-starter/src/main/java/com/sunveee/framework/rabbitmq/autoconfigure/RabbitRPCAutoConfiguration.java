package com.sunveee.framework.rabbitmq.autoconfigure;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import com.sunveee.framework.rabbitmq.rpc.consumer.*;
import com.sunveee.framework.rabbitmq.rpc.consumer.aspect.RPCConsumerExpireMessageFilterAspect;
import com.sunveee.framework.rabbitmq.rpc.consumer.aspect.RPCConsumerLogPrintAspect;
import com.sunveee.framework.rabbitmq.rpc.producer.RPCRabbitTemplate;
import com.sunveee.framework.rabbitmq.rpc.properties.RPCConsumerProperties;
import com.sunveee.framework.rabbitmq.rpc.properties.RPCProducerProperties;

@Configuration
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@EnableConfigurationProperties({ RPCProducerProperties.class, RPCConsumerProperties.class })
public class RabbitRPCAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RPCRabbitTemplate rpcRabbitTemplate(ConnectionFactory connectionFactory, RPCProducerProperties rpcProducerProperties) {
        return new RPCRabbitTemplate(connectionFactory, rpcProducerProperties);
    }

    @Bean("rpcSimpleErrorHandler")
    @ConditionalOnMissingBean
    public RPCSimpleErrorHandler rpcSimpleErrorHandler() {
        return new RPCSimpleErrorHandler();
    }

    @Bean("rpcBizErrorHandler")
    @ConditionalOnMissingBean
    public RPCBizErrorHandler rpcBizErrorHandler() {
        return new RPCBizErrorHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public RPCConsumerLogPrintAspect rpcConsumerLogPrintAspect() {
        return new RPCConsumerLogPrintAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public RPCConsumerExpireMessageFilterAspect rpcConsumerExpireMessageFilterAspect() {
        return new RPCConsumerExpireMessageFilterAspect();
    }

}
