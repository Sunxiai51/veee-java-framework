package com.sunveee.framework.rabbitmq.rpc.consumer.anno;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCConsumerMethod {

    boolean paramPrint() default true;

    boolean resultPrint() default true;
}
