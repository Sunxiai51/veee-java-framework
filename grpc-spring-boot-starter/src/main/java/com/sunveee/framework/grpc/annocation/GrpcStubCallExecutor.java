package com.sunveee.framework.grpc.annocation;

import com.sunveee.framework.grpc.aspect.GrpcStubCallExecuteAspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GrpcStubCallExecutor
 *
 * @author SunVeee
 * @date 2022/3/29 14:44
 * @see GrpcStubCallExecuteAspect
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcStubCallExecutor {
}
