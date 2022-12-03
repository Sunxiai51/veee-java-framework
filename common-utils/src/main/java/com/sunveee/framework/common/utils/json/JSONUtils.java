package com.sunveee.framework.common.utils.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.*;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.Message;
import com.sunveee.framework.common.utils.protobuf.ProtobufJsonUtils;

import java.util.ArrayList;
import java.util.List;

public class JSONUtils {

    protected static final int DEFAULT_PARSER_FEATURE;
    private static final int DEFAULT_GENERATE_FEATURE;

    static {
        int features = JSON.DEFAULT_PARSER_FEATURE;
        features |= SerializerFeature.WriteEnumUsingName.getMask();
        // features |= SerializerFeature.UseSingleQuotes.getMask();
        features |= SerializerFeature.WriteDateUseDateFormat.getMask();
        DEFAULT_PARSER_FEATURE = features;
    }

    static {
        int features = JSON.DEFAULT_GENERATE_FEATURE;
        features |= SerializerFeature.WriteEnumUsingName.getMask();
        features |= SerializerFeature.WriteDateUseDateFormat.getMask();
        DEFAULT_GENERATE_FEATURE = features;
        SerializeConfig.getGlobalInstance().propertyNamingStrategy = PropertyNamingStrategy.CamelCase;
    }

    /**
     * 把Java对象序列化成Json字符串
     * <p>
     * 如果入参为集合类型且包含item为{@link Message}对象，请使用{@link ProtobufJsonUtils#toJsonArrayString(List)}代替
     *
     * @param javaObject
     * @return
     * @see {@link ProtobufJsonUtils#toJsonArrayString(List)}
     */
    public static String toJSONString(Object javaObject) {
        if (javaObject instanceof Message) {
            return ProtobufJsonUtils.toJsonString((Message) javaObject);
        }
        if (javaObject == null) {
            return null;
        }
        // 如果是json则直接返回
        if (javaObject instanceof JSONObject || javaObject instanceof String) {
            return javaObject.toString();
        }
        return JSON.toJSONString(javaObject, DEFAULT_GENERATE_FEATURE);
    }

    /**
     * 把Java对象序列化成Json字符串(驼峰转下划线)
     *
     * @param javaObject
     * @return
     */
    public static String toJSONStringWithCamelToUnderline(Object javaObject) {
        if (javaObject instanceof Message) {
            return ProtobufJsonUtils.toJsonString((Message) javaObject);
        }
        if (javaObject == null) {
            return null;
        }
        // 如果是json则直接返回
        if (javaObject instanceof JSONObject || javaObject instanceof String) {
            return javaObject.toString();
        }

        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        return JSON.toJSONString(javaObject, config, null, null, DEFAULT_GENERATE_FEATURE, SerializerFeature.WriteEnumUsingName);
    }

    /**
     * 把Json字符串转换成指定Java对象
     *
     * @param <T>
     * @param jsonString
     * @param clazz
     * @return
     */
    public static <T> T parseObject(String jsonString, Class<T> clazz) {
        if (Message.class.isAssignableFrom(clazz)) {
            return ProtobufJsonUtils.fromJsonString(jsonString, clazz);
        }
        if (jsonString == null) {
            return null;
        }
        return JSON.parseObject(jsonString, clazz, DEFAULT_PARSER_FEATURE);
    }

    /**
     * 序列化字符串到List对象
     *
     * @param <T>
     * @param text
     * @param clazz
     * @return
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (Message.class.isAssignableFrom(clazz)) {
            return ProtobufJsonUtils.fromJsonArrayString(text, clazz);
        }
        if (text == null) {
            return null;
        }

        List<T> list;

        DefaultJSONParser parser = new DefaultJSONParser(text, new JSONScanner(text, DEFAULT_PARSER_FEATURE), ParserConfig.getGlobalInstance());
        JSONLexer lexer = parser.lexer;
        int token = lexer.token();
        if (token == JSONToken.NULL) {
            lexer.nextToken();
            list = null;
        } else if (token == JSONToken.EOF && lexer.isBlankInput()) {
            list = null;
        } else {
            list = new ArrayList<T>();
            parser.parseArray(clazz, list);
            parser.handleResovleTask(list);
        }
        parser.close();

        return list;
    }

}
