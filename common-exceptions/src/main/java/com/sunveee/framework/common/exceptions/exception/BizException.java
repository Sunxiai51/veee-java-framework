package com.sunveee.framework.common.exceptions.exception;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class BizException extends BaseException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, String detail) {
        super(message, detail);
    }

    @Override
    public String toString() {
        return "BizException [message=" + getMessage() + ", detail=" + getDetail() + "]";
    }

}
