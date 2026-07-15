package com.phper666.sforce.api.sdk.builder;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public class StringSoqlBuilder<T> extends AbstractSoqlBuilder<T, String, StringSoqlBuilder<T>> {
    public StringSoqlBuilder() {
    }

    public StringSoqlBuilder(Class<T> entityClass) {
        select.initiate(entityClass);
    }

    @Override
    protected StringSoqlBuilder<T> instance() {
        return new StringSoqlBuilder<>();
    }
}
