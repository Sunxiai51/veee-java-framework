package com.sunveee.framework.rabbitmq.simple.producer.exception;

import lombok.Getter;

@Getter
public class MessageReturnedException extends RuntimeException {

    private String exchange;
    private String routingKey;

    public MessageReturnedException(String exchange, String routingKey) {
        super();
        this.exchange = exchange;
        this.routingKey = routingKey;
    }
}
