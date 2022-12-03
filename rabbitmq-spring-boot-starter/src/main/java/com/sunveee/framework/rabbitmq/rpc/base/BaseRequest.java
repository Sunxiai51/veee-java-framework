package com.sunveee.framework.rabbitmq.rpc.base;

import java.io.Serializable;

import lombok.Data;

@Data
public class BaseRequest implements Serializable {

    private String requestId;

    private Long timestamp;

}
