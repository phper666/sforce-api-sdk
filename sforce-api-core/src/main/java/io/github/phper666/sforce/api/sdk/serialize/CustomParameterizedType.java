package io.github.phper666.sforce.api.sdk.serialize;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ParameterizedType implementation for generic type resolution.
 *
 * @author Yuzhao.Li
 */
public class CustomParameterizedType implements ParameterizedType {

    private final Class rawType;
    private final Type[] args;

    public CustomParameterizedType(Class rawType, Type[] args) {
        this.rawType = rawType;
        this.args = args != null ? args : new Type[0];
    }

    @Override
    public Type[] getActualTypeArguments() {
        return args;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}
