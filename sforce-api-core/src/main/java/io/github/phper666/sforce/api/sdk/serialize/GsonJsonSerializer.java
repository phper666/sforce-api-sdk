package io.github.phper666.sforce.api.sdk.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Gson-based implementation of JsonSerializer.
 *
 * @param <T> the type parameter for deserialization target type
 * @author Yuzhao.Li
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class GsonJsonSerializer<T> implements JsonSerializer<T> {

    private final static Gson GSON_INSTANCE;

    static {
        GSON_INSTANCE = new GsonBuilder().enableComplexMapKeySerialization().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
    }

    private final static GsonJsonSerializer SERIALIZER;

    private GsonJsonSerializer() {
    }

    static {
        SERIALIZER = new GsonJsonSerializer();
    }

    public static GsonJsonSerializer INSTANCE() {
        return SERIALIZER;
    }

    public String toJson(Object o) {
        return GSON_INSTANCE.toJson(o);
    }

    public T fromJson(String json, Class<T> aClass) {
        return GSON_INSTANCE.fromJson(json, aClass);
    }

    public T fromJson(String json, Type type) {
        return GSON_INSTANCE.fromJson(json, type);
    }

    public <T> List<T> fromJsonList(String jsonStr, Class<T> tClass) {
        Type type = new CustomParameterizedType(List.class, new Type[]{tClass});
        return GSON_INSTANCE.fromJson(jsonStr, type);
    }
}
