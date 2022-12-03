package com.sunveee.framework.rabbitmq.producer.example.controller;

import com.sunveee.framework.rabbitmq.rpc.util.ResponseCodeConstant;
import org.springframework.amqp.UncategorizedAmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.sunveee.framework.rabbitmq.producer.example.vo.*;
import com.sunveee.framework.rabbitmq.rpc.exception.ReplyTimeoutException;
import com.sunveee.framework.rabbitmq.rpc.producer.RPCRabbitTemplate;
import com.sunveee.framework.rabbitmq.rpc.simple.BizRequest;
import com.sunveee.framework.rabbitmq.rpc.simple.BizResponse;
import com.sunveee.framework.rabbitmq.rpc.util.BizRequestBuilder;

@RestController
@RequestMapping("/rabbitmq")
public class RPCProducerController {

    @Autowired
    private RPCRabbitTemplate rpcRabbitTemplate;

    @RequestMapping("/rpcInvoke/string")
    public String rpcString(@RequestParam String routingKey, @RequestParam String message) {
        String response = rpcRabbitTemplate.rpcInvoke(routingKey, message);
        return response;
    }

    @RequestMapping("/rpcInvoke/biz")
    public String rpcBiz(@RequestBody MyRequestParam requestParam) {
        BizRequest<MyRequestParam> request = BizRequestBuilder.build(requestParam);
        try {
            BizResponse<MyResponseData> response = rpcRabbitTemplate.rpcBizInvoke("queue_test_rpc_biz", request, MyResponseData.class);
            return JSON.toJSONString(response);
        } catch (ReplyTimeoutException e) {
            return "消费端响应超时,超时时间:" + e.getTimeout() + "ms";
        }
    }

    @RequestMapping("/rpcInvoke/performanceTest")
    public String performanceTest(@RequestBody PerformanceTestRequestParam requestParam) {
        if (requestParam.isRandomSwitch()) {
            final double random = Math.random(); // [0, 1)
            // 50%【正常返回】
            if (random < 0.5) { // [0, 0.5)
                requestParam.setReturnType("NORMAL");
                // 25%【正常返回 & 立即返回】
                if (random < 0.25) { // [0, 0.25)
                    requestParam.setSleep(false);
                }
                // 25%【正常返回 & 模拟执行】
                else { // [0.25, 0.5)
                    requestParam.setSleep(true);
                    // 12.5%【正常返回 & 模拟执行 & 执行1s】
                    if (random < 0.375) { // [0.25, 0.375)
                        requestParam.setSleepTimeMills(1000L);
                    }
                    // 12.5%【正常返回 & 模拟执行 & 执行4s】
                    else { // [0.375, 0.5)
                        requestParam.setSleepTimeMills(1000L);
                    }
                }
            }

            // 50%异常返回
            else { // [0.5, 1)
                requestParam.setReturnType("EXCEPTION");
                // 25%【异常返回 & 立即返回】
                if (random < 0.75) { // [0.5, 0.75)
                    requestParam.setSleep(false);
                }
                // 25%【异常返回 & 模拟执行】
                else { // [0.75, 1)
                    requestParam.setSleep(true);
                    // 12.5%【异常返回 & 模拟执行 & 执行1s】
                    if (random < 0.875) { // [0.75, 0.875)
                        requestParam.setSleepTimeMills(1000L);
                    }
                    // 12.5%【异常返回 & 模拟执行 & 执行4s】
                    else { // [0.875, 1)
                        requestParam.setSleepTimeMills(1000L);
                    }
                }
            }
        }

        BizRequest<PerformanceTestRequestParam> request = BizRequestBuilder.build(requestParam);
        try {
            BizResponse<MyResponseData> response = rpcRabbitTemplate.rpcBizInvoke("queue_test_rpc_performance", request, MyResponseData.class);
            if (null == response.getCode()) {
                return "Null response";
            }
            switch (response.getCode()) {
                case ResponseCodeConstant.SUCCESS:
                    return "SUCCESS";
                default:
                    return response.getCode();
            }
        } catch (UncategorizedAmqpException e) {
            if (null != e.getCause() && e.getCause() instanceof ReplyTimeoutException) {
                return "ReplyTimeout";
            } else {
                throw e;
            }
        }
    }

}
