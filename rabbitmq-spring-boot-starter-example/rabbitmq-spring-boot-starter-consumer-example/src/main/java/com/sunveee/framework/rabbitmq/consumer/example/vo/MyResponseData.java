package com.sunveee.framework.rabbitmq.consumer.example.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class MyResponseData implements Serializable {

    private String value;

}
