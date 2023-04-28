package com.sunveee.framework.common.exceptions.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BaseException extends RuntimeException {

    private String detail;

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "BaseException [message=" + getMessage() + ", detail=" + detail + "]";
    }

}
