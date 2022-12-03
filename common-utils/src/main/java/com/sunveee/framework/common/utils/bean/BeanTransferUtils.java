package com.sunveee.framework.common.utils.bean;

import static org.apache.commons.beanutils.BeanUtils.copyProperty;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.apache.commons.beanutils.*;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.Message;
import com.sunveee.framework.common.utils.json.JSONUtils;
import com.sunveee.framework.common.utils.protobuf.ProtobufJsonUtils;

public class BeanTransferUtils {

    /**
     * 通过Json正反序列化转换Bean
     *
     * @param  source
     * @param  destClass
     * @param  <T>
     * @return
     */
    public static <T> T transferBean(Object source, Class<T> destClass) {
        // 解析
        String jsonString = JSONUtils.toJSONString(source);
        // 转换
        return JSONUtils.parseObject(jsonString, destClass);
    }

    /**
     * 把集合转换成指定类型的集合
     *
     * @param  sourceList
     * @param  destClass
     * @param  <T>
     * @return
     */
    public static <T> List<T> transferBeanList(Collection<?> sourceList, Class<T> destClass) {
        String jsonString = JSONUtils.toJSONString(sourceList);
        return JSONUtils.parseArray(jsonString, destClass);
    }

    // 利用json来进行枚举转换
    public static <T> List<T> transferBeanList(Object sourceList, Class<T> destClass) {
        if (sourceList == null || StringUtils.isEmpty(sourceList.toString())) {
            return null;
        }
        String jsonstr = JSONUtils.toJSONString(sourceList);
        return JSONArray.parseArray(jsonstr, destClass);
    }

    /**
     * 对象拷贝 数据对象空值不拷贝到目标对象
     *
     * @param databean
     * @param tobean   copy
     */
    public static void copyBeanNotNull2Bean(Object databean, Object tobean) {
        PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(databean);
        for (int i = 0; i < origDescriptors.length; i++) {
            PropertyDescriptor origDescriptor = origDescriptors[i];
            String name = origDescriptor.getName();
//          String type = origDescriptor.getPropertyType().toString();
            if ("class".equals(name)) {
                continue; // No point in trying to set an object's class
            }
            if (PropertyUtils.isReadable(databean, name) &&
                    PropertyUtils.isWriteable(tobean, name)) {
                try {
                    Object value = PropertyUtils.getSimpleProperty(databean, name);
                    if (value != null) {
                        copyProperty(tobean, name, value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("copyBeanNotNull2Bean error: " + e.getMessage(), e);
                }
            }
        }

    }

    /**
     * 把orig和dest相同属性的value复制到dest中
     * 
     * @param dest
     * @param orig
     */
    public static void copyBean2Bean(Object dest, Object orig) {
        // Validate existence of the specified beans
        if (dest == null) {
            throw new IllegalArgumentException("No destination bean specified");
        }
        if (orig == null) {
            throw new IllegalArgumentException("No origin bean specified");
        }
        // Copy the properties, converting as necessary
        if (orig instanceof DynaBean) {
            DynaProperty origDescriptors[] = ((DynaBean) orig).getDynaClass().getDynaProperties();
            for (int i = 0; i < origDescriptors.length; i++) {
                String name = origDescriptors[i].getName();
                if (PropertyUtils.isWriteable(dest, name)) {
                    Object value = ((DynaBean) orig).get(name);
                    try {
                        copyProperty(dest, name, value);
                    } catch (Exception e) {
                        throw new RuntimeException("copyBean2Bean error: " + e.getMessage(), e);
                    }
                }
            }
        } else if (orig instanceof Map) {
            Iterator<?> names = ((Map<?, ?>) orig).keySet().iterator();
            while (names.hasNext()) {
                String name = (String) names.next();
                if (PropertyUtils.isWriteable(dest, name)) {
                    Object value = ((Map<?, ?>) orig).get(name);
                    try {
                        copyProperty(dest, name, value);
                    } catch (Exception e) {
                        throw new RuntimeException("copyBean2Bean error: " + e.getMessage(), e);
                    }
                }
            }
        } else
        /* if (orig is a standard JavaBean) */
        {
            PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(orig);
            for (int i = 0; i < origDescriptors.length; i++) {
                String name = origDescriptors[i].getName();
//              String type = origDescriptors[i].getPropertyType().toString();
                if ("class".equals(name)) {
                    continue; // No point in trying to set an object's class
                }
                if (PropertyUtils.isReadable(orig, name) &&
                        PropertyUtils.isWriteable(dest, name)) {
                    try {
                        Object value = PropertyUtils.getSimpleProperty(orig, name);
                        copyProperty(dest, name, value);
                    } catch (Exception e) {
                        throw new RuntimeException("copyBean2Bean error: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * 将Bean中属性与Map内的key相同的内容复制到Map中
     * 
     * @param map      Map
     * @param bean     Object
     * @param copyNull Boolean
     */
    public static void copyBean2Map(Map<String, Object> map, Object bean, Boolean copyNull) {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
        for (int i = 0; i < pds.length; i++) {
            PropertyDescriptor pd = pds[i];
            String propname = pd.getName();
            try {
                Object propvalue = PropertyUtils.getSimpleProperty(bean, propname);
                if (Objects.nonNull(propvalue) || copyNull) {
                    map.put(propname, propvalue);
                }
            } catch (Exception e) {
                throw new RuntimeException("copyBean2Bean error: " + e.getMessage(), e);
            }
        }
    }

    public static void copyBean2Map(Map<String, Object> map, Object bean) {
        copyBean2Map(map, bean, false);
    }

    /**
     * 将Map内的key与Bean中属性相同的内容复制到BEAN中
     * 
     * @param bean       Object
     * @param properties Map
     * @param copyNull   Boolean
     */
    public static void copyMap2Bean(Object bean, Map<?, ?> properties, Boolean copyNull) {
        // Do nothing unless both arguments have been specified
        if ((bean == null) || (properties == null)) {
            return;
        }
        // Loop through the property name/value pairs to be set
        for (Object o : properties.keySet()) {
            String name = (String) o;
            // Identify the property name and value(s) to be assigned
            if (name == null) {
                continue;
            }
            Object value = properties.get(name);
            try {
                Class<?> clazz = PropertyUtils.getPropertyType(bean, name);
                if (null == clazz) {
                    continue;
                }
                String className = clazz.getName();
                if (className.equalsIgnoreCase("java.sql.Timestamp")) {
                    if (value == null || value.equals("")) {
                        continue;
                    }
                }
                if (Objects.nonNull(value) || copyNull) {
                    BeanUtils.setProperty(bean, name, value);
                }
            } catch (NoSuchMethodException ignored) {
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("copyMap2Bean error: " + e.getMessage(), e);
            }
        }
    }

    public static void copyMap2Bean(Object bean, Map<?, ?> properties) {
        copyMap2Bean(bean, properties, false);
    }

    /**
     * 通过Json正反序列化将POJO转换为ProtoMessage
     * 
     * @param  <T>
     * @param  pojo
     * @param  protoMessageClass
     * @return
     */
    public static <T extends Message> T pojoToProtoMessage(Object pojo, Class<T> protoMessageClass) {
        String jsonString = JSONUtils.toJSONString(pojo);
        return ProtobufJsonUtils.fromJsonString(jsonString, protoMessageClass);
    }

    /**
     * 通过Json正反序列化将ProtoMessage转换为POJO
     * 
     * @param  <T>
     * @param  protoMessage
     * @param  pojoClass
     * @return
     */
    public static <T> T protoMessageToPojo(Message protoMessage, Class<T> pojoClass) {
        String jsonString = ProtobufJsonUtils.toJsonString(protoMessage);
        return JSONUtils.parseObject(jsonString, pojoClass);
    }
}
