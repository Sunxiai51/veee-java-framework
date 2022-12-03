package com.sunveee.framework.common.exceptions.utils;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.sunveee.framework.common.exceptions.exception.LogicException;

public class LogicAssertUtil {

    public static void isTrue(boolean value, String message) {
        isTrue(value, message, null);
    }

    public static void isTrue(boolean value, String message, String detail) {
        if (!value) {
            throw new LogicException(message, detail);
        }
    }

    public static void notNull(Object value, String message) {
        notNull(value, message, null);
    }

    public static void notNull(Object value, String message, String detail) {
        if (null == value) {
            throw new LogicException(message, detail);
        }
    }

    public static void isNull(Object value, String message) {
        isNull(value, message, null);
    }

    public static void isNull(Object value, String message, String detail) {
        if (null != value) {
            throw new LogicException(message, detail);
        }
    }

    public static void hasText(String value, String message) {
        hasText(value, message, null);
    }

    public static void hasText(String value, String message, String detail) {
        if (StringUtils.isBlank(value)) {
            throw new LogicException(message, detail);
        }
    }

    public static void notEmpty(Collection<?> coll, String message) {
        notEmpty(coll, message, null);
    }

    public static void notEmpty(Collection<?> coll, String message, String detail) {
        if (CollectionUtils.isEmpty(coll)) {
            throw new LogicException(message, detail);
        }
    }

    public static void isEmpty(Collection<?> coll, String message) {
        isEmpty(coll, message, null);
    }

    public static void isEmpty(Collection<?> coll, String message, String detail) {
        if (CollectionUtils.isNotEmpty(coll)) {
            throw new LogicException(message, detail);
        }
    }
}
