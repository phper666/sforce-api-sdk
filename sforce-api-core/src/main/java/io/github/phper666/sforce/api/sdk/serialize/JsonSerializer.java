package io.github.phper666.sforce.api.sdk.serialize;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Interface for JSON serialization and deserialization.
 *
 * @param <T> the type parameter for deserialization target type
 * @author Yuzhao.Li
 */
public interface JsonSerializer<T> {

    String toJson(Object o);

    T fromJson(String json, Class<T> tClass);

    T fromJson(String json, Type type);

    <T> List<T> fromJsonList(String jsonStr, Class<T> tClass);
}
