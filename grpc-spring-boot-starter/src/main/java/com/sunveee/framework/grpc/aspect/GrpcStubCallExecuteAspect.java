package com.sunveee.framework.grpc.aspect;

import com.sunveee.framework.grpc.utils.StubExecuteExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * GrpcStubCallExecuteAspect
 * 处理grpc客户端调用的异常
 *
 * @author SunVeee
 * @date 2022/3/29 14:49
 */
@Aspect
@Slf4j
public class GrpcStubCallExecuteAspect {

    @Pointcut("@annotation(com.sunveee.framework.grpc.annocation.GrpcStubCallExecutor)||" +
            "(@within(com.sunveee.framework.grpc.annocation.GrpcStubCallExecutor)" +
            "&&execution(public * *(..)))")
    public void grpcStubCallExecutor() {
    }

    @Around("grpcStubCallExecutor()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable throwable) {
            StubExecuteExceptionHandler.handleRecognizableThrowable(throwable);
            throw throwable;
        }
        return proceed;
    }

}
