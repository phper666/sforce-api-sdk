package io.github.phper666.sforce.api.sdk.builder;

/**
 * SOQL builder for string-based column references.
 *
 * @author Yuzhao.Li
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
