package com.sunveee.framework.rabbitmq.consumer.example.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.sunveee.framework.rabbitmq.consumer.example.vo.PerformanceTestRequestParam;
import com.sunveee.framework.rabbitmq.simple.consumer.anno.SimpleConsumerMethod;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SimpleConsumer {

    // 仅声明队列

    @SimpleConsumerMethod
    @RabbitListener(queuesToDeclare = @Queue("queue_test_simple"), containerFactory = "simpleConsumerContainerFactory")
    public void simpleReceive(String content) {
        // do things...

    }

    // 声明队列，同时声明binding

    @SimpleConsumerMethod
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "queue_test_simple", durable = "true"), exchange = @Exchange(value = "test_exchange", ignoreDeclarationExceptions = "true"), key = "test"), containerFactory = "simpleConsumerContainerFactory")
    public void simpleReceive(@Payload String content, Channel channel, Message message) {
        // do things...

    }

    /**
     * 性能测试
     * 
     * @param content
     * @return
     * @throws InterruptedException
     */
    @SimpleConsumerMethod
    @RabbitListener(queuesToDeclare = @Queue("queue_test_simple_performance"), containerFactory = "simpleConsumerContainerFactory")
    public void forPerformanceTest(String content) throws InterruptedException {

        // 解析请求
        PerformanceTestRequestParam param = JSON.parseObject(content, PerformanceTestRequestParam.class);
        log.info("param: {}.", param);

        if (param.isSleep()) {
            Thread.sleep(param.getSleepTimeMills());
        }

        // 返回
        switch (param.getReturnType()) {
            case "EXCEPTION":
                throw new RuntimeException("throw exception ### " + param.toString());
            case "NORMAL":
            default:
                ;
        }

    }

}
