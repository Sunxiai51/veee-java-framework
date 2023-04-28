package com.sunveee.framework.common.utils.protobuf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ProtobufJsonUtils.java
 * <p>
 * Proto Message的json序列化/反序列化
 *
 * @author SunVeee
 * @version 2022-01-11 19:02:53
 */
public class ProtobufJsonUtils {
    private static final JsonFormat.Printer printer;
    private static final JsonFormat.Parser parser;

    static {
        JsonFormat.TypeRegistry registry = JsonFormat.TypeRegistry.newBuilder().build();

        printer = JsonFormat
                .printer()
                .usingTypeRegistry(registry)
                .includingDefaultValueFields()
                .omittingInsignificantWhitespace();

        parser = JsonFormat
                .parser()
                .ignoringUnknownFields()
                .usingTypeRegistry(registry);
    }

    public static String toJsonString(Message message) {
        if (message == null) {
            return "";
        }

        try {
            return printer.print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJsonString(String jsonObjectString, Class<T> clazz) {
        if (!Message.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("该方法不支持非com.google.protobuf.Message类型");
        }
        if (StringUtils.isBlank(jsonObjectString)) {
            return null;
        }

        try {
            final java.lang.reflect.Method method = clazz.getMethod("newBuilder");
            final Message.Builder builder = (Message.Builder) method.invoke(null);

            parser.merge(jsonObjectString, builder);

            return (T) builder.build();
        } catch (Exception e) {
            throw new RuntimeException("ProtobufJsonUtils.fromJsonObjectString() exception, class: " + clazz + ", json: " + jsonObjectString, e);
        }
    }

    public static String toJsonArrayString(List<? extends MessageOrBuilder> messageList) {
        if (messageList == null) {
            return "";
        }
        if (messageList.isEmpty()) {
            return "[]";
        }

        try {
            StringBuilder builder = new StringBuilder(1024);
            builder.append("[");
            for (MessageOrBuilder message : messageList) {
                printer.appendTo(message, builder);
                builder.append(",");
            }
            return builder.deleteCharAt(builder.length() - 1).append("]").toString();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static <T> List<T> fromJsonArrayString(String jsonArrayString, Class<T> clazz) {
        if (!Message.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("该方法不支持非com.google.protobuf.Message类型");
        }

        if (StringUtils.isBlank(jsonArrayString)) {
            return Collections.emptyList();
        }

        final JSONArray jsonArray = JSON.parseArray(jsonArrayString);

        final List<T> resultList = new ArrayList<>(jsonArray.size());

        for (int i = 0; i < jsonArray.size(); i++) {
            resultList.add(fromJsonString(jsonArray.getString(i), clazz));
        }

        return resultList;
    }

    public static String toJsonMapString(Map<?, ? extends Message> messageMap) {
        if (messageMap == null) {
            return "";
        }
        if (messageMap.isEmpty()) {
            return "{}";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        messageMap.forEach((k, v) -> {
            sb.append("\"").append(JSON.toJSONString(k)).append("\":").append(toJsonString(v)).append(",");
        });
        sb.deleteCharAt(sb.length() - 1).append("}");
        return sb.toString();
    }

    public static <K, V extends Message> Map<K, V> fromJsonMapString(String json, Class<K> keyClazz, Class<V> valueClazz) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }

        final JSONObject jsonObject = JSON.parseObject(json);

        final Map<K, V> map = Maps.newHashMapWithExpectedSize(jsonObject.size());
        for (String key : jsonObject.keySet()) {
            final K k = JSONObject.parseObject(key, keyClazz);
            final V v = fromJsonString(jsonObject.getString(key), valueClazz);

            map.put(k, v);
        }

        return map;
    }

}
