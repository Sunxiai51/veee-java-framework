package com.sunveee.framework.common.exceptions.exception;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class LogicException extends BaseException {

    public LogicException(String message) {
        super(message);
    }

    public LogicException(String message, String detail) {
        super(message, detail);
    }

    @Override
    public String toString() {
        return "LogicException [message=" + getMessage() + ", detail=" + getDetail() + "]";
    }

}
