package com.sunveee.framework.rabbitmq.consumer.example.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class MyRequestParam implements Serializable {

    private int intParam;
    private boolean booleanParam;
    private String stringParam;

}
