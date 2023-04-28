package com.sunveee.framework.rabbitmq.simple.consumer.aspect;

import java.lang.reflect.Method;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;

import com.sunveee.framework.rabbitmq.simple.consumer.anno.SimpleConsumerMethod;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费端日志打印与消费耗时统计 RPCConsumerLogPrintAspect.java
 *
 * @author SunVeee
 * @version 2021-01-20 11:56:08
 */
@Slf4j
@Aspect
public class SimpleConsumerLogPrintAspect {
    public static final String MDC_THREAD_ID = "THREAD_ID";

    @Pointcut("@annotation(com.sunveee.framework.rabbitmq.simple.consumer.anno.SimpleConsumerMethod)")
    public void simpleConsumerMethod() {
    }

    @Around("simpleConsumerMethod()")
    public void logPrint(ProceedingJoinPoint pjp) throws Throwable {
        // 设置MDC_THREAD_ID
        final String threadId = UUID.randomUUID().toString().replaceAll("-", "");
        MDC.put(MDC_THREAD_ID, threadId);
        log.info("Generate MDC_THREAD_ID: {}.", threadId);

        // 消息接收时间
        final long receiveTime = System.currentTimeMillis();

        // 消费类
        Class<?> clazz = pjp.getTarget().getClass();
        String clazzName = clazz.getSimpleName();

        // 消费方法
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String methodName = ms.getName();
        Method method = clazz.getDeclaredMethod(methodName, ms.getParameterTypes());

        // 请求参数
        String[] parameterNames = ms.getParameterNames();
        Object[] parameterValues = pjp.getArgs();

        // 方法注解
        SimpleConsumerMethod simpleConsumerMethod = method.getAnnotation(SimpleConsumerMethod.class);

        boolean printParam = null != simpleConsumerMethod && simpleConsumerMethod.paramPrint();
        boolean printCost = null != simpleConsumerMethod && simpleConsumerMethod.costPrint();

        if (printParam) {
            log.info("[{}.{}] received params: {}.", clazzName, methodName, buildParameterKVStr(parameterNames, parameterValues));
        }

        try {
            pjp.proceed();
        } finally {
            // 请求完成时间
            final long endTime = System.currentTimeMillis();
            if (printCost) {
                log.info("[{}.{}] finished, cost {}ms.", clazzName, methodName, endTime - receiveTime);
            }
            MDC.remove(MDC_THREAD_ID);
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
