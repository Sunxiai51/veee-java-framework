package com.sunveee.framework.rabbitmq.rpc.base;

import java.io.Serializable;

import lombok.Data;

@Data
public class BaseResponse implements Serializable {

    private String code;

    private String message;

}
