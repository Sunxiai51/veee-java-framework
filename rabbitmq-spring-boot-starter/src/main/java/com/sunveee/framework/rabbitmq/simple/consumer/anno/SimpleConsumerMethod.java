package com.sunveee.framework.rabbitmq.simple.consumer.anno;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleConsumerMethod {

    boolean paramPrint() default true;

    boolean costPrint() default true;
}
