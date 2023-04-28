package com.sunveee.framework.common.utils.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class FastJsonUtils {

    /**
     * 统一处理JSON枚举转换
     *
     * @param  javaObject
     * @return
     */
    public static JSONObject toJSON(Object javaObject) {
        return toJSON(javaObject, JSONUtils.DEFAULT_PARSER_FEATURE);
    }

    /**
     * 统一处理JSON枚举转换
     *
     * @param  javaObject
     * @return
     */
    public static JSONObject toJSON(Object javaObject, int feature) {
        // 如果是json则直接返回
        if (javaObject instanceof JSONObject) {
            return (JSONObject) javaObject;
        }

        String text = JSONUtils.toJSONString(javaObject);
        Object obj = JSON.parse(text, feature);
        if (obj instanceof JSONObject) {
            return (JSONObject) obj;
        }
        return (JSONObject) JSON.toJSON(obj);
    }

}
