package com.sunveee.framework.rabbitmq.producer.example.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class PerformanceTestRequestParam implements Serializable {

    private boolean randomSwitch = false;

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
