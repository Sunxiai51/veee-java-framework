package com.sunveee.framework.rabbitmq.rpc.consumer.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import com.sunveee.framework.rabbitmq.rpc.consumer.anno.RPCBizConsumerMethod;
import com.sunveee.framework.rabbitmq.rpc.consumer.anno.RPCConsumerMethod;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费端日志打印与消费耗时统计 RPCConsumerLogPrintAspect.java
 *
 * @author SunVeee
 * @version 2021-01-20 11:56:08
 */
@Slf4j
@Aspect
public class RPCConsumerLogPrintAspect {

    @Pointcut("@annotation(com.sunveee.framework.rabbitmq.rpc.consumer.anno.RPCBizConsumerMethod)")
    public void rpcBizConsumerMethod() {
    }

    @Pointcut("@annotation(com.sunveee.framework.rabbitmq.rpc.consumer.anno.RPCConsumerMethod)")
    public void rpcConsumerMethod() {
    }

    @Around("rpcBizConsumerMethod() || rpcConsumerMethod()")
    public Object logPrint(ProceedingJoinPoint pjp) throws Throwable {
        // 消息接收时间
        final long receiveTime = System.currentTimeMillis();

        // 消费类
        Class<?> clazz = pjp.getTarget().getClass();
        String clazzName = clazz.getSimpleName();

        // 消费方法
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String methodName = ms.getName();
//        Method method = clazz.getDeclaredMethod(methodName, ms.getParameterTypes());
        Method method = ms.getMethod();

        // 请求参数
        String[] parameterNames = ms.getParameterNames();
        Object[] parameterValues = pjp.getArgs();

        // 方法注解
        RPCConsumerMethod rpcConsumerMethod = method.getAnnotation(RPCConsumerMethod.class);
        RPCBizConsumerMethod rpcBizConsumerMethod = method.getAnnotation(RPCBizConsumerMethod.class);

        boolean printParam = (null != rpcConsumerMethod && rpcConsumerMethod.paramPrint()) ||
                (null != rpcBizConsumerMethod && rpcBizConsumerMethod.paramPrint());
        boolean printResult = (null != rpcConsumerMethod && rpcConsumerMethod.resultPrint()) ||
                (null != rpcBizConsumerMethod && rpcBizConsumerMethod.resultPrint());

        if (printParam) {
            log.info("[{}.{}] received params: {}.", clazzName, methodName, buildParameterKVStr(parameterNames, parameterValues));
        }

        Object result = null;
        try {
            result = pjp.proceed();
            return result;
        } finally {
            // 请求完成时间
            final long endTime = System.currentTimeMillis();
            if (printResult) {
                log.info("[{}.{}] finished, result: [{}], cost {}ms.", clazzName, methodName, result, endTime - receiveTime);
            }
        }

    }

    private static String buildParameterKVStr(String[] parameterNames, Object[] parameterValues) {
        StringBuilder paramsBuilder = new StringBuilder();
        paramsBuilder.append("[");
        if (parameterNames != null && parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals("bindingResult")) {
                    break;
                }
                paramsBuilder.append(parameterNames[i]).append("=").append(parameterValues[i]);
                if (i < parameterNames.length - 1) {
                    paramsBuilder.append(", ");
                }

            }
        }
        paramsBuilder.append("]");
        return paramsBuilder.toString();
    }
}
