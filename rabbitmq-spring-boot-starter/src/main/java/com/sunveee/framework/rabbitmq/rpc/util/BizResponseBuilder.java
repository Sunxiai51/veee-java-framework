package com.sunveee.framework.rabbitmq.rpc.util;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import com.sunveee.framework.rabbitmq.rpc.simple.BizResponse;

public class BizResponseBuilder {

    public static <D extends Serializable> BizResponse<D> build(String code, String message, D data) {
        BizResponse<D> result = new BizResponse<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static BizResponse<? extends Serializable> build(String code, String message) {
        return build(code, message, "");
    }

    public static <D extends Serializable> BizResponse<D> buildSuccessResponse(D data) {
        return build(ResponseCodeConstant.SUCCESS, "成功", data);
    }

    public static <D extends Serializable> BizResponse<D> buildBizFailResponse() {
        return build(ResponseCodeConstant.BIZ_FAILED, "业务失败", null);
    }

    public static <D extends Serializable> BizResponse<D> buildErrorResponse() {
        return build(ResponseCodeConstant.SYSTEM_ERROR, "系统异常", null);
    }

    @SuppressWarnings("unchecked")
    public static <D extends Serializable> BizResponse<D> fromJson(String json, Class<D> dataClazz) {
        String dataStr = JSONObject.parseObject(json).getString("data");
        D data = JSONObject.parseObject(dataStr, dataClazz);

        BizResponse<D> result = JSONObject.parseObject(json, BizResponse.class);
        result.setData(data);
        return result;
    }

}
