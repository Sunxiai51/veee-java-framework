package com.sunveee.framework.rabbitmq.rpc.exception;

import lombok.Getter;

@Getter
public class ReplyTimeoutException extends RuntimeException {

    private long timeout;

    public ReplyTimeoutException(long timeout, String message) {
        super(message);
        this.timeout = timeout;
    }

}
