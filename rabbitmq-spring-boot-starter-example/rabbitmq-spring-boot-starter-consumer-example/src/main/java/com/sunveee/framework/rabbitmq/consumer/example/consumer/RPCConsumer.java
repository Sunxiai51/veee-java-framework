package com.sunveee.framework.rabbitmq.consumer.example.consumer;

import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.sunveee.framework.rabbitmq.consumer.example.vo.*;
import com.sunveee.framework.rabbitmq.rpc.consumer.anno.RPCBizConsumerMethod;
import com.sunveee.framework.rabbitmq.rpc.consumer.anno.RPCConsumerMethod;
import com.sunveee.framework.rabbitmq.rpc.simple.BizRequest;
import com.sunveee.framework.rabbitmq.rpc.util.BizRequestBuilder;
import com.sunveee.framework.rabbitmq.rpc.util.BizResponseBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RPCConsumer {

    /**
     * Simple
     * 
     * @param content
     * @return
     */
    @RPCConsumerMethod
    @RabbitListener(queuesToDeclare = @Queue("queue_test_rpc_string"), errorHandler = "rpcSimpleErrorHandler")
    public String receiveString(String content) {

        // do things...

        // 返回
        return "handle result ### " + content;
    }

    /**
     * Biz
     * 
     * @param content
     * @return
     */
    @RPCBizConsumerMethod
    @RabbitListener(queuesToDeclare = @Queue("queue_test_rpc_biz"), errorHandler = "rpcBizErrorHandler")
    public String receiveBizRequest(String content) {

        // 解析请求
        BizRequest<MyRequestParam> request = BizRequestBuilder.fromJson(content, MyRequestParam.class);
        log.info("request: {}.", request);

        // do things...

        // 返回
        MyResponseData data = new MyResponseData();
        data.setValue("handle data ### " + request.getParam().toString());
        return JSON.toJSONString(BizResponseBuilder.buildSuccessResponse(data));
    }

    /**
     * 性能测试
     * 
     * @param content
     * @return
     * @throws InterruptedException
     */
    @RPCBizConsumerMethod(expire = 1000)
    @RabbitListener(queuesToDeclare = @Queue("queue_test_rpc_performance"), errorHandler = "rpcBizErrorHandler")
    public String forPerformanceTest(String content) throws InterruptedException {

        // 解析请求
        BizRequest<PerformanceTestRequestParam> request = BizRequestBuilder.fromJson(content, PerformanceTestRequestParam.class);
        log.info("request: {}.", request);

        PerformanceTestRequestParam param = request.getParam();

        if (param.isSleep()) {
            Thread.sleep(param.getSleepTimeMills());
        }

        // 返回
        switch (param.getReturnType()) {
            case "EXCEPTION":
                throw new RuntimeException("throw exception ### " + param.toString());
            case "NORMAL":
            default:
                MyResponseData data = new MyResponseData();
                data.setValue("handle data ### " + param.toString());
                return JSON.toJSONString(BizResponseBuilder.buildSuccessResponse(data));
        }

    }

}
