package com.sunveee.framework.rabbitmq.rpc.producer;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.alibaba.fastjson.JSON;
import com.sunveee.framework.rabbitmq.rpc.exception.ReplyTimeoutException;
import com.sunveee.framework.rabbitmq.rpc.properties.RPCProducerProperties;
import com.sunveee.framework.rabbitmq.rpc.simple.BizRequest;
import com.sunveee.framework.rabbitmq.rpc.simple.BizResponse;
import com.sunveee.framework.rabbitmq.rpc.util.BizResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RPCRabbitTemplate extends RabbitTemplate {

    private RPCProducerProperties rpcProperties;

    public RPCRabbitTemplate(ConnectionFactory connectionFactory, RPCProducerProperties rpcProperties) {
        super(connectionFactory);
        this.rpcProperties = rpcProperties;
        this.setMandatory(true);
        this.setReplyTimeout(rpcProperties.getReplyTimeout());
        this.setUseDirectReplyToContainer(false);

        log.info("RPCRabbitTemplate initialized with rpcProperties: {}.", rpcProperties);
    }

    @Override
    protected void replyTimedOut(String correlationId) {
        log.warn("Timeout of correlationId: {}.", correlationId);
        throw new ReplyTimeoutException(rpcProperties.getReplyTimeout(), correlationId);
    }

    public String rpcInvoke(String routingKey, String content) {
        if (rpcProperties.isInvokeLogEnabled()) {
            log.info("RPC invoke, routingKey: {}, content: {}.", routingKey, content);
        }

        Object res = this.convertSendAndReceive(routingKey, content);
        String response;
        if (res instanceof byte[]) {
            try {
                response = new String((byte[]) res, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else if (res instanceof String) {
            response = (String) res;
        } else {
            log.warn("Unknown response type from routingKey[{}].", routingKey);
            response = JSON.toJSONString(res);
        }

        if (rpcProperties.isInvokeLogEnabled()) {
            log.info("RPC invoke response: {}.", response);
        }

        return response;
    }

    public <D extends Serializable> BizResponse<D> rpcBizInvoke(String routingKey, BizRequest<?> request, Class<D> dataClazz) {
        String responseStr = rpcInvoke(routingKey, JSON.toJSONString(request));
        try {
            return BizResponseBuilder.fromJson(responseStr, dataClazz);
        } catch (Exception e) {
            log.error("parse response exception, responseStr: {}", responseStr, e);
            throw e;
        }
    }

}
