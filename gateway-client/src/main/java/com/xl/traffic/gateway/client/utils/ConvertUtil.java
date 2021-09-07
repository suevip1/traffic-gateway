package com.xl.traffic.gateway.client.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 转换工具类
 *
 * @author: xl
 * @date: 2021/9/7
 **/
public class ConvertUtil {

    /**
     * 自动识别泛型转换
     *
     * @param obj
     * @return: java.lang.Class<?>
     * @author: xl
     * @date: 2021/9/7
     **/
    public static Class<?> convertClassType(Object obj) {
        Type[] types = obj.getClass().getGenericInterfaces();
        if (types[0] instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) types[0];
            if (parameterized != null) {
                Class<?> clazz = (Class<?>) parameterized.getActualTypeArguments()[0];
                if (clazz != null) {

                    return clazz;
                }
            }
        }
        /**没有泛型 默认string*/
        return String.class;
    }

}
