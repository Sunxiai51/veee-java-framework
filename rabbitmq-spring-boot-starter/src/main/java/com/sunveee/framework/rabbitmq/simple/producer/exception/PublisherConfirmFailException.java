package com.sunveee.framework.rabbitmq.simple.producer.exception;

import lombok.Getter;

@Getter
public class PublisherConfirmFailException extends RuntimeException {

    private String exchange;
    private String routingKey;

    public PublisherConfirmFailException(String message, String exchange, String routingKey) {
        super(message);
        this.exchange = exchange;
        this.routingKey = routingKey;
    }
}
