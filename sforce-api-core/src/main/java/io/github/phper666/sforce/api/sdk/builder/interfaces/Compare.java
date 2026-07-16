package io.github.phper666.sforce.api.sdk.builder.interfaces;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * 查询条件封装
 * <p>比较值</p>
 * @author Yuzhao.Li
 */
public interface Compare<Children, R> extends Serializable {

    /**
     * ignore
     */
    default Children eq(R column, Object value) {
        return eq(true, column, value);
    }

    /**
     * 等于 =
     *
     * @param condition 执行条件
     * @param column    字段
     * @param value     值
     * @return children
     */
    Children eq(boolean condition, R column, Object value);

    /**
     * ignore
     */
    default Children neq(R column, Object value) {
        return neq(true, column, value);
    }

    /**
     * 不等于 !=;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param value     值
     * @return children
     */
    Children neq(boolean condition, R column, Object value);

    /**
     * ignore
     */
    default Children gt(R column, Object value) {
        return gt(true, column, value);
    }

    /**
     * 大于 >;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param value     值
     * @return children
     */
    Children gt(boolean condition, R column, Object value);

    /**
     * ignore
     */
    default Children ge(R column, Object value) {
        return ge(true, column, value);
    }

    /**
     * 大于等于 >=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param value     值
     * @return children
     */
    Children ge(boolean condition, R column, Object value);

    /**
     * ignore
     */
    default Children lt(R column, Object value) {
        return lt(true, column, value);
    }

    /**
     * 小于 &lt;;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param value     值
     * @return children
     */
    Children lt(boolean condition, R column, Object value);

    /**
     * ignore
     */
    default Children le(R column, Object value) {
        return le(true, column, value);
    }

    /**
     * 小于等于 &lt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param value     值
     * @return children
     */
    Children le(boolean condition, R column, Object value);

    default Children eqMultiSelect(R column, Object... values) {
        return eqMultiSelect(true, column, values);
    }
    /**
     * picklists类型,等于 = 'AAA;BBB'
     * 例: .eqMultiSelect(Conversation::getIsOwner,"AAA","BBB")-> CXG__IsOwner__c = 'AAA;BBB'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    值数组
     */
    default Children eqMultiSelect(boolean condition, R column, Object... values) {
        return eqMultiSelect(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .toList());
    }

    default Children eqMultiSelect(R column, Collection<?> coll) {
        return eqMultiSelect(true, column, coll);
    }

    Children eqMultiSelect(boolean condition, R column, Collection<?> coll);

    default Children neqMultiSelect(R column, Object... values) {
        return neqMultiSelect(true, column, values);
    }

    default Children neqMultiSelect(boolean condition, R column, Object... values) {
        return neqMultiSelect(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .toList());
    }

    default Children neqMultiSelect(R column, Collection<?> coll) {
        return neqMultiSelect(true, column, coll);
    }

    /**
     * picklists类型,不等于 != 'AAA;BBB'
     * 例: .neqMultiSelect(Conversation::getIsOwner,"AAA","BBB")-> CXG__IsOwner__c != 'AAA;BBB'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      值集合
     */
    Children neqMultiSelect(boolean condition, R column, Collection<?> coll);

    default Children includes(R column, Object... values) {
        return includes(true, column, values);
    }

    /**
     * picklists类型 包含 INCLUDES ('AAA;BBB','CCC')
     * 例: .includes(Conversation::getExternalId,"ccc","aaa;bbb")-> CXG__ExternalId__c INCLUDES ('AAA;BBB','CCC')
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    值数组
     */
    default Children includes(boolean condition, R column, Object... values) {
        return includes(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .toList());
    }

    default Children includes(R column, Collection<?> coll) {
        return includes(true, column, coll);
    }

    Children includes(boolean condition, R column, Collection<?> coll);

    default Children excludes(R column, Object... values) {
        return excludes(true, column, values);
    }

    /**
     * picklists类型 不包含 EXCLUDES ('AAA;BBB','CCC')
     * 例: .excludes(Conversation::getExternalId,"ccc","aaa;bbb")-> CXG__ExternalId__c EXCLUDES ('AAA;BBB','CCC')
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    值数组
     */
    default Children excludes(boolean condition, R column, Object... values) {
        return excludes(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .toList());
    }

    default Children excludes(R column, Collection<?> coll) {
        return excludes(true, column, coll);
    }

    Children excludes(boolean condition, R column, Collection<?> coll);
}
