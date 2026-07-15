package com.phper666.sforce.api.sdk.builder.utils;

import com.google.gson.annotations.SerializedName;
import com.phper666.sforce.api.sdk.builder.ObjectName;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.phper666.sforce.api.sdk.builder.StringPool.*;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public final class SoqlUtil {
    public static final String VALUE_FORMAT = "'%s'";
    private static final Map<Class<?>, List<String>> caches = new HashMap<>();
    public static final String TIME_FORMAT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+\\-]\\d{2}:\\d{2}";
    private static final Pattern TIME_PATTERN = Pattern.compile(TIME_FORMAT);
    private static String customObjectNamespace;

    public static void setGlobalCustomObjectNamespace(String namespace) {
        customObjectNamespace = namespace;
    }

    public static String appendNamespace(String field) {
        if (customObjectNamespace != null && !customObjectNamespace.isEmpty()
                && !field.startsWith(customObjectNamespace + "__")
                && field.endsWith("__c")) {
            return customObjectNamespace + "__" + field;
        }
        return field;
    }

    public static List<String> getFields(Class<?> clazz) {
        var selectFields = caches.get(clazz);
        if (selectFields != null) {
            return selectFields;
        }
        selectFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(SoqlUtil::getFieldName)
                .toList();
        caches.put(clazz, selectFields);
        return selectFields;
    }

    public static String getObjectName(Class<?> clazz) {
        var objectName = clazz.getAnnotation(ObjectName.class);
        var name = objectName != null ? objectName.value() : clazz.getSimpleName();
        return appendNamespace(name);
    }

    public static String getFieldName(Field field) {
        var annotation = field.getAnnotation(SerializedName.class);
        var name = annotation != null ? annotation.value() : field.getName();
        return appendNamespace(name);
    }

    public static String getValuesFormat(Object... values) {
        var valuesFormat = Arrays.stream(values).map(SoqlUtil::getValueFormat).collect(Collectors.joining(COMMA));
        return LEFT_BRACKET + valuesFormat + RIGHT_BRACKET;
    }

    public static String getValuesFormat(Collection<?> values) {
        var valuesFormat = values.stream().map(SoqlUtil::getValueFormat).collect(Collectors.joining(COMMA));
        return LEFT_BRACKET + valuesFormat + RIGHT_BRACKET;
    }

    public static String getInValueFormat(String inValue) {
        if (inValue == null) {
            inValue = NULL;
        }
        if (inValue.startsWith(LEFT_BRACKET) && inValue.endsWith(RIGHT_BRACKET)) {
            return inValue;
        }
        return LEFT_BRACKET + inValue + RIGHT_BRACKET;
    }

    public static String getValueFormat(Object value) {
        if (value == null) {
            return NULL;
        }
        if (value instanceof String) {
            if (TIME_PATTERN.matcher(String.valueOf(value)).matches()) {
                try {
                    return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8.displayName());
                } catch (Exception e) {
                    return value.toString();
                }
            } else {
                return VALUE_FORMAT.formatted(value);
            }
        }
        return value.toString();
    }

    public static String getMultiSelectValue(String... values) {
        return String.join(SEMICOLON, values);
    }

    public static String getMultiSelectValue(Collection<?> values) {
        return values.stream().map((Function<Object, String>) Object::toString).collect(Collectors.joining(SEMICOLON));
    }
}
