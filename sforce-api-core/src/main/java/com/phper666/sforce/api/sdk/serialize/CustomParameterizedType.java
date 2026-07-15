package com.phper666.sforce.api.sdk.serialize;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
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
