package com.sunveee.framework.rabbitmq.simple.producer;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.connection.CorrelationData.Confirm;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSON;
import com.sunveee.framework.rabbitmq.simple.producer.callback.SimpleConfirmCallback;
import com.sunveee.framework.rabbitmq.simple.producer.callback.SimpleReturnCallback;
import com.sunveee.framework.rabbitmq.simple.producer.exception.*;
import com.sunveee.framework.rabbitmq.simple.properties.SimpleProducerProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * SimpleRabbitTemplate.java
 * <p>
 * 封装消息发送API，包含特性：
 * <li>开启confirm与return机制
 * <li>发送异常处理：可重试场景进行延迟重试（例如超时），不可重试场景告警（例如缺少exchange），其它未知场景告警+重试
 * <li>日志打印
 *
 * @author SunVeee
 * @version 2021-01-11 20:01:15
 */
@Slf4j
public class SimpleRabbitTemplate extends RabbitTemplate {

    private SimpleProducerProperties globalProperties;

    public SimpleRabbitTemplate(SimpleCachingConnectionFactory connectionFactory, SimpleProducerProperties simpleProperties) {
        super(connectionFactory);
        this.globalProperties = simpleProperties;
        // 消息return机制
        this.setMandatory(true); // 开启
        this.setReturnCallback(new SimpleReturnCallback()); // 设置returnCallback

        // 消息confirm机制
        this.setConfirmCallback(new SimpleConfirmCallback()); // 设置confirmCallback

        log.info("SimpleRabbitTemplate initialized with simpleProperties: {}.", simpleProperties);
    }

    /**
     * 发送Object（使用默认的全局配置）
     * 
     * @param exchange
     * @param routingKey
     * @param message
     */
    public void simpleSendObject(String exchange, String routingKey, Object message)
            throws MessageReturnedException, PublisherConfirmFailException, SendTimeoutException {
        simpleSendObject(exchange, routingKey, message, null);
    }

    /**
     * 发送Object（使用自定义配置）
     * 
     * @param exchange
     * @param routingKey
     * @param message
     * @param sendConfig
     */
    public void simpleSendObject(String exchange, String routingKey, Object message, SimpleProducerSendConfig sendConfig)
            throws MessageReturnedException, PublisherConfirmFailException, SendTimeoutException {
        simpleSend(exchange, routingKey, JSON.toJSONString(message), sendConfig);
    }

    /**
     * 发送String（使用默认的全局配置）
     * 
     * @param exchange
     * @param routingKey
     * @param message
     */
    public void simpleSend(String exchange, String routingKey, String message)
            throws MessageReturnedException, PublisherConfirmFailException, SendTimeoutException {
        simpleSend(exchange, routingKey, message, null);
    }

    /**
     * 发送String（使用自定义配置）
     * 
     * @param exchange
     * @param routingKey
     * @param message
     * @param sendConfig
     */
    public void simpleSend(String exchange, String routingKey, String message, SimpleProducerSendConfig sendConfig)
            throws MessageReturnedException, PublisherConfirmFailException, SendTimeoutException {
        SimpleProducerProperties sendProperties = customizeSendConfig(sendConfig, globalProperties);
        log.info("Simple sending message to exchange[{}] and routingKey[{}], message content: {}, sendProperties: {}.", exchange, routingKey, message, sendProperties);
        try {
            executeSend(exchange, routingKey, message, sendProperties);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Exception occured when waiting ack.", e);
        } catch (TimeoutException e) {
            handleTimeout(exchange, routingKey, message, sendProperties);
        }
    }

    private void executeSend(String exchange, String routingKey, String message, SimpleProducerProperties sendProperties) throws InterruptedException, ExecutionException, TimeoutException {
        final long sendTimeout = actualSendTimeoutValue(sendProperties);

        // 使用CorrelationData接收confirm与return信息
        CorrelationData cd = new CorrelationData();
        cd.setId(generateCorrelationId()); // 每次发送消息创建唯一的correlationId
        this.convertAndSend(exchange, routingKey, message, cd);

        Confirm confirm = cd.getFuture().get(sendTimeout, TimeUnit.MILLISECONDS);
        if (confirm.isAck()) {
            handleAckTrue(exchange, routingKey, message, sendProperties, cd);
        } else {
            handleAckFalse(confirm, exchange, routingKey, message, sendProperties);
        }
    }

    private void handleAckTrue(String exchange, String routingKey, String message, SimpleProducerProperties sendProperties, CorrelationData cd) {
        if (sendProperties.isReturnCallbackListenerEnabled()) {
            // 判断是否发生了消息return
            if (null != cd.getReturnedMessage()) {
                handleMessageReturned(exchange, routingKey);
            } else {
                return;
            }
        } else {
            return;
        }
    }

    /**
     * 处理发送超时
     * <p>
     * 将根据发送参数决定是否进行重试，开启重试时将阻塞当前线程
     * 
     * @param exchange
     * @param routingKey
     * @param message
     * @param sendProperties
     */
    private void handleTimeout(String exchange, String routingKey, String message, SimpleProducerProperties sendProperties) {
        log.warn("Simple send timeout, message: {}, sendProperties: {}.", message, sendProperties);
        final int retries = sendProperties.getRetries();

        // 重试次数<=0时，不重试，直接抛出超时异常
        if (retries <= 0) {
            throw new SendTimeoutException();
        }
        // 重试次数>0时，按指定重试间隔执行重试
        else {
            int retryCount = 0;
            double[] intervals = parseRetryInterval(sendProperties);
            while (retryCount < retries) {
                try {
                    // 阻塞当前线程指定间隔时间
                    Thread.sleep((long) (1000L * intervals[retryCount]));

                    // 执行重新发送逻辑
                    log.info("To retry[{}/{}] send message to exchange[{}] and routingKey[{}], message content: {}.", retryCount + 1, retries, exchange, routingKey, message);
                    executeSend(exchange, routingKey, message, sendProperties);
                    log.info("Retry[{}/{}] success.", retryCount + 1, retries);
                    return;
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Exception occured when waiting ack.", e);
                } catch (TimeoutException e) {
                    log.info("Retry[{}/{}] timeout.", retryCount + 1, retries);
                    retryCount++;
                    continue;
                }
            }
            // 指定次数重试依然超时，抛出异常
            throw new SendTimeoutException();
        }

    }

    private void handleMessageReturned(String exchange, String routingKey) {
        // 消息return时重试可能无法修复，直接抛出异常
        throw new MessageReturnedException(exchange, routingKey);
    }

    private void handleAckFalse(Confirm confirm, String exchange, String routingKey, String message, SimpleProducerProperties sendProperties) {
        // ack=false失败时重试可能无法修复，直接抛出异常
        throw new PublisherConfirmFailException(confirm.getReason(), exchange, routingKey);
    }

    private SimpleProducerProperties customizeSendConfig(SimpleProducerSendConfig sendConfig, SimpleProducerProperties globalProperties2) {
        SimpleProducerProperties result = new SimpleProducerProperties();
        BeanUtils.copyProperties(globalProperties2, result);
        if (null != sendConfig) {
            Optional.ofNullable(sendConfig.getInitialSendTimeout()).ifPresent(result::setInitialSendTimeout);
            Optional.ofNullable(sendConfig.getRetries()).ifPresent(result::setRetries);
            Optional.ofNullable(sendConfig.getRetryInterval()).ifPresent(result::setRetryInterval);
            Optional.ofNullable(sendConfig.getReturnCallbackListenerEnabled()).ifPresent(result::setReturnCallbackListenerEnabled);
        }
        return result;
    }

    /**
     * 实际发送超时，保证：
     * <li>发送超时不超过全局最大超时时间
     * <li>发送超时不低于1
     * 
     * @param sendProperties
     * @return
     */
    private long actualSendTimeoutValue(SimpleProducerProperties sendProperties) {
        return Math.max(1, Math.min(sendProperties.getInitialSendTimeout(), globalProperties.getGlobalMaxSendTimeout()));
    }

    private static double[] parseRetryInterval(SimpleProducerProperties sendProperties) {
        double[] result = new double[sendProperties.getRetries()];
        String[] intervals = sendProperties.getRetryInterval().split(",");

        int settledIndex = 0;
        for (int i = 0; i < result.length; i++) {
            if (i < intervals.length) {
                result[i] = Double.valueOf(intervals[i]);
                settledIndex = i;
            } else {
                result[i] = result[settledIndex];
            }
        }

        return result;
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
