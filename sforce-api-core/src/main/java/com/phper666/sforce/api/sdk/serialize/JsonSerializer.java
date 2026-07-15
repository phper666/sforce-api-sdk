package com.phper666.sforce.api.sdk.serialize;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public interface JsonSerializer<T> {

    String toJson(Object o);

    T fromJson(String json, Class<T> tClass);

    T fromJson(String json, Type type);

    <T> List<T> fromJsonList(String jsonStr, Class<T> tClass);
}
