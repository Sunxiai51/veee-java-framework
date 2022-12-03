package com.sunveee.framework.rabbitmq.rpc.util;

import java.io.Serializable;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.sunveee.framework.rabbitmq.rpc.simple.BizRequest;

public class BizRequestBuilder {

    public static <P extends Serializable> BizRequest<P> build(String requestId, Long timestamp, P param) {
        BizRequest<P> result = new BizRequest<P>();
        result.setRequestId(requestId);
        result.setTimestamp(timestamp);
        result.setParam(param);
        return result;
    }

    public static <P extends Serializable> BizRequest<P> build(String requestId, P param) {
        return build(requestId, defaultTimestamp(), param);
    }

    public static <P extends Serializable> BizRequest<P> build(P param) {
        return build(defaultRequestId(), defaultTimestamp(), param);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Serializable> BizRequest<P> fromJson(String json, Class<P> paramClazz) {
        String paramStr = JSONObject.parseObject(json).getString("param");
        P param = JSONObject.parseObject(paramStr, paramClazz);

        BizRequest<P> result = JSONObject.parseObject(json, BizRequest.class);
        result.setParam(param);
        return result;
    }

    private static String defaultRequestId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private static long defaultTimestamp() {
        return System.currentTimeMillis();
    }

}
