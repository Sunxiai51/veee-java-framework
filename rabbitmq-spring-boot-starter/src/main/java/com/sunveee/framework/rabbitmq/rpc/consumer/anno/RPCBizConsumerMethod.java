package com.sunveee.framework.rabbitmq.rpc.consumer.anno;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RPCConsumerMethod
public @interface RPCBizConsumerMethod {

    /**
     * 过期时间（毫秒数）
     * <li>{@code <0} 表示全部不过期
     * <li>{@code =0} 表示全部过期，将丢弃所有消息
     * <li>{@code >0} 表示指定过期时间范围，将丢弃有效时间段以外的消息
     */
    long expire() default 5000;

    boolean paramPrint() default true;

    boolean resultPrint() default true;
}
