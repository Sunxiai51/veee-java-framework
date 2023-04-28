package com.sunveee.framework.common.utils.bean;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectUtils {

    public static Class<?> getSuperClassGenericType(Class<?> clazz, int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                return !(params[index] instanceof Class) ? Object.class : (Class<?>) params[index];
            } else {
                return Object.class;
            }
        }
    }

    public static boolean isImplInterface(Class<?> interClass, Class<?> impClass) {
        return interClass != null && impClass != null ? interClass.isAssignableFrom(impClass) : false;
    }

    public static Class<?> getSuperGenericType(Class<?> clazz) {
        return getSuperClassGenericType(clazz, 0);
    }
}