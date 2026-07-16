package io.github.phper666.sforce.api.sdk.internal;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.phper666.sforce.api.sdk.builder.utils.SoqlUtil.appendNamespace;

/**
 * Gson TypeAdapterFactory for custom namespace serialization.
 *
 * @author Yuzhao.Li
 */
public class CustomObjectTypeFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(this, type);
        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                AppendCustomNamespace appendCustomNamespace = getAnnotation(value.getClass());
                if (appendCustomNamespace != null) {
                    appendNamespaceWrite(gson, out, value);
                } else {
                    delegateAdapter.write(out, value);
                }
            }

            @Override
            @SneakyThrows
            @SuppressWarnings("unchecked")
            public T read(JsonReader in) {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                Class<T> objectClass = (Class<T>) type.getRawType().getClassLoader().loadClass(type.getType().getTypeName());
                AppendCustomNamespace appendCustomNamespace = getAnnotation(objectClass);
                if (appendCustomNamespace != null) {
                    return appendNamespaceRead(gson, in, objectClass);
                } else {
                    return delegateAdapter.read(in);
                }
            }
        }.nullSafe();
    }

    @SneakyThrows
    private <T> T appendNamespaceRead(Gson gson, JsonReader in, Class<T> objectClass) {
        T instance = objectClass.newInstance();
        Map<String, Field> boundFieldMap = boundFieldMap(objectClass);
        in.beginObject();
        while (in.hasNext()) {
            Field boundField = boundFieldMap.get(in.nextName());
            if (boundField == null) {
                in.skipValue();
            } else {
                Field field = instance.getClass().getDeclaredField(boundField.getName());
                field.setAccessible(true);
                Object fieldValue = gson.fromJson(in, field.getGenericType());
                field.set(instance, fieldValue);
            }
        }
        in.endObject();
        return instance;
    }

    private <T> Map<String, Field> boundFieldMap(Class<T> objectClass) {
        return Arrays.stream(objectClass.getDeclaredFields()).map(field -> {
            SerializedName annotation = field.getAnnotation(SerializedName.class);
            if (annotation == null) {
                return new BoundField(field.getName(),field);
            } else {
                return new BoundField(appendNamespace(annotation.value()), field);
            }
        }).collect(Collectors.toMap(BoundField::getFieldName, BoundField::getField));
    }

    @SneakyThrows
    private <T> void appendNamespaceWrite(Gson gson, JsonWriter out, T value) {
        out.beginObject();
        List<Field> fields = getAllFields(value.getClass());
        for (Field field : fields) {
            SerializedName annotation = field.getAnnotation(SerializedName.class);
            if (annotation != null) {
                String name = annotation.value();
                field.setAccessible(true);
                Object fieldValue = field.get(value);
                if (fieldValue != null) {
                    out.name(appendNamespace(name)).jsonValue(gson.toJson(fieldValue));
                }
            }
        }
        out.endObject();
    }

    @Getter
    @Setter
    private static class BoundField {
        private String fieldName;
        private Field field;

        public BoundField(String fieldName, Field field) {
            this.fieldName = fieldName;
            this.field = field;
        }
    }

    private AppendCustomNamespace getAnnotation(Class<?> clazz) {
        AppendCustomNamespace appendCustomNamespace = clazz.getAnnotation(AppendCustomNamespace.class);
        if (appendCustomNamespace != null) {
            return appendCustomNamespace;
        }
        while (clazz != null && appendCustomNamespace == null) {
            appendCustomNamespace = clazz.getAnnotation(AppendCustomNamespace.class);
            clazz = clazz.getSuperclass();
        }
        return appendCustomNamespace;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        ArrayList<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
