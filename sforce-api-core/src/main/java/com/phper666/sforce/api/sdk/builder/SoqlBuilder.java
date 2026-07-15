package com.phper666.sforce.api.sdk.builder;

import com.phper666.sforce.api.sdk.builder.interfaces.SFunction;
import com.phper666.sforce.api.sdk.builder.interfaces.SoqlSegment;

import java.util.Arrays;

import static com.phper666.sforce.api.sdk.builder.utils.LambdaUtils.resolveToFieldName;
import static com.phper666.sforce.api.sdk.builder.utils.SoqlUtil.appendNamespace;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public class SoqlBuilder<T> extends AbstractSoqlBuilder<T, SFunction<T, ?>, SoqlBuilder<T>> {

    public SoqlBuilder() {
    }

    public SoqlBuilder(Class<T> entityClass) {
        select.initiate(entityClass);
    }

    /**
     * 用于设置一些不是entity字段中的字段查询,可结合groupBy使用
     * @param columns 自己定义的字段名,如: MIN(NumberOfEmployees) min
     */
    public SoqlBuilder<T> select(String... columns) {
        var selectedFields = Arrays.stream(columns).map(column -> (SoqlSegment) () -> column).toList();
        select.addAll(selectedFields);
        return this;
    }

    @Override
    protected String columnToString(SFunction<T, ?> column) {
        return appendNamespace(resolveToFieldName(column));
    }

    @Override
    protected SoqlBuilder<T> instance() {
        return new SoqlBuilder<>();
    }
}
