package com.sunveee.framework.arranger.util;

public class AssertUtil {

    public static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new RuntimeException(message);
        }
    }

    public static void assertNotNull(Object value, String message) {
        if (null == value) {
            throw new RuntimeException(message);
        }
    }
}
