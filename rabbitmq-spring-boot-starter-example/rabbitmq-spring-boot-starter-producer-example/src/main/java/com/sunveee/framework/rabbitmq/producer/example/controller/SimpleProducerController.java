package com.sunveee.framework.rabbitmq.producer.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.sunveee.framework.rabbitmq.producer.example.vo.MyRequestParam;
import com.sunveee.framework.rabbitmq.producer.example.vo.PerformanceTestRequestParam;
import com.sunveee.framework.rabbitmq.simple.producer.SimpleProducerSendConfig;
import com.sunveee.framework.rabbitmq.simple.producer.SimpleRabbitTemplate;

@RestController
@RequestMapping("/rabbitmq")
public class SimpleProducerController {

    @Autowired
    private SimpleRabbitTemplate simpleRabbitTemplate;

    @RequestMapping("/simple/send")
    public String send(@RequestParam String exchange, @RequestParam String routingKey, @RequestParam String message) {
        simpleRabbitTemplate.simpleSend(exchange, routingKey, message);
        return "done";
    }

    @RequestMapping("/simple/sendObject")
    public String sendObject(@RequestParam String exchange, @RequestParam String routingKey, @RequestParam String message) {
        MyRequestParam param = new MyRequestParam();
        param.setStringParam(message);
        simpleRabbitTemplate.simpleSendObject(exchange, routingKey, param);
        return "done";
    }

    @RequestMapping("/simple/sendWithConfig")
    public String sendWithConfig(
            @RequestParam String exchange, @RequestParam String routingKey, @RequestParam String message,
            @RequestParam Integer retries, @RequestParam Long initialSendTimeout,
            @RequestParam String retryInterval, @RequestParam Boolean returnCallbackListenerEnabled) {
        SimpleProducerSendConfig sendConfig = SimpleProducerSendConfig.builder()
                .initialSendTimeout(initialSendTimeout)
                .retries(retries)
                .retryInterval(retryInterval)
                .returnCallbackListenerEnabled(returnCallbackListenerEnabled)
                .build();
        simpleRabbitTemplate.simpleSend(exchange, routingKey, message, sendConfig);
        return "done";
    }

    @RequestMapping("/simple/performanceTest")
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

        simpleRabbitTemplate.simpleSendObject(null, "queue_test_simple_performance", requestParam);

        return JSON.toJSONString(requestParam);
    }

}
