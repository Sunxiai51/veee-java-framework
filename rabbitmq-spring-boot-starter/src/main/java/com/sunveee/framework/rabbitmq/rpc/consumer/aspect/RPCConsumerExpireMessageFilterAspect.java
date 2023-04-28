package com.sunveee.framework.rabbitmq.rpc.consumer.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import com.alibaba.fastjson.JSONObject;
import com.sunveee.framework.rabbitmq.rpc.base.BaseRequest;
import com.sunveee.framework.rabbitmq.rpc.consumer.anno.RPCBizConsumerMethod;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费端 RPCConsumerExpireMessageFilterAspect.java
 *
 * @author SunVeee
 * @version 2021-01-20 11:56:36
 */
@Slf4j
@Aspect
public class RPCConsumerExpireMessageFilterAspect {

    @Pointcut("@annotation(com.sunveee.framework.rabbitmq.rpc.consumer.anno.RPCBizConsumerMethod)")
    public void rpcBizConsumerMethod() {
    }

    @Around("rpcBizConsumerMethod()")
    public Object expireMessageFilter(ProceedingJoinPoint pjp) throws Throwable {
        // 接收消息时间
        final long receiveTime = System.currentTimeMillis();

        // 消费类
        Class<?> clazz = pjp.getTarget().getClass();

        // 消费方法
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String methodName = ms.getName();
        Method method = clazz.getDeclaredMethod(methodName, ms.getParameterTypes());

        // 请求参数
        Object[] parameterValues = pjp.getArgs();
        BaseRequest baseRequest = transferBaseRequest(parameterValues);

        // 方法注解
        RPCBizConsumerMethod rpcBizConsumerMethod = method.getAnnotation(RPCBizConsumerMethod.class);

        // 请求解析失败，跳过过滤逻辑
        if (null == baseRequest) {
            log.warn("Cannot transfer message to BaseRequest, RPCConsumerExpireMessageFilterAspect would not work.");
            return pjp.proceed();
        }
        // 请求解析成功，但不存在目标注解，跳过过滤逻辑（该逻辑分支理论上不存在，因为该注解为切点）
        else if (null == rpcBizConsumerMethod) {
            log.error("Unknown logical branch, RPCConsumerExpireMessageFilterAspect would not work.");
            return pjp.proceed();
        }
        // 请求解析成功，且存在目标注解
        else {
            // 未设置消息发送时间
            if (null == baseRequest.getTimestamp()) {
                log.warn("BaseRequest's timestamp unset, RPCConsumerExpireMessageFilterAspect would not work.");
                return pjp.proceed();
            }
            // 未设置过期时间，或消息发送时间在过期时间之内，未过期
            else if (rpcBizConsumerMethod.expire() < 0 || baseRequest.getTimestamp() > receiveTime - rpcBizConsumerMethod.expire()) {
                return pjp.proceed();
            }
            // 消息过期，直接丢弃
            else {
                log.info("Message expired, discard it.");
                return null;
            }
        }

    }

    private BaseRequest transferBaseRequest(Object[] parameterValues) {
        if (parameterValues.length >= 1 && parameterValues[0] instanceof String) {
            try {
                String input = (String) parameterValues[0];
                BaseRequest result = JSONObject.parseObject(input, BaseRequest.class);
                return result;
            } catch (Exception e) {
                // do nothing and return null
            }
        }
        return null;

    }

}
