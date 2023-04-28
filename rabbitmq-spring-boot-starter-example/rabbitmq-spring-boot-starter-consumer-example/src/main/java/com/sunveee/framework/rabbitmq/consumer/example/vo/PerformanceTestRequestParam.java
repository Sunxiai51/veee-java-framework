package com.sunveee.framework.rabbitmq.consumer.example.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class PerformanceTestRequestParam implements Serializable {

    /**
     * 是否睡眠
     */
    private boolean sleep = false;
    private long sleepTimeMills = 0;

    /**
     * 返回类型
     * <li>NORMAL
     * <li>EXCEPTION
     */
    private String returnType = "NORMAL";

}
