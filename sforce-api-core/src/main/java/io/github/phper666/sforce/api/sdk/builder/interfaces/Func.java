package io.github.phper666.sforce.api.sdk.builder.interfaces;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * 查询条件封装
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@SuppressWarnings("unchecked")
public interface Func<Children, R> extends Serializable {

    /**
     * ignore
     */
    default Children in(R column, Collection<?> coll) {
        return in(true, column, coll);
    }

    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <p> 如果集合为 empty 则不会进行 soql 拼接 </p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合 coll size &lt;=200
     * @return children
     */
    Children in(boolean condition, R column, Collection<?> coll);

    /**
     * ignore
     */
    default Children in(R column, Object... values) {
        return in(true, column, values);
    }

    /**
     * 字段 IN (v0, v1, ...)
     * <p>例: in("id", 1, 2, 3, 4, 5)</p>
     *
     * <p> 如果动态数组为 empty 则不会进行 soql 拼接 </p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组 size &lt;=200
     * @return children
     */
    default Children in(boolean condition, R column, Object... values) {
        return in(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .toList());
    }

    /**
     * ignore
     */
    default Children notIn(R column, Collection<?> coll) {
        return notIn(true, column, coll);
    }

    /**
     * 字段 NOT IN (value.get(0), value.get(1), ...)
     * <p>例: notIn("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合 size &lt;=200
     * @return children
     */
    Children notIn(boolean condition, R column, Collection<?> coll);

    /**
     * ignore
     */
    default Children notIn(R column, Object... value) {
        return notIn(true, column, value);
    }

    /**
     * 字段 NOT IN (v0, v1, ...)
     * <p>例: notIn("id", 1, 2, 3, 4, 5)</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组 size &lt;=200
     * @return children
     */
    default Children notIn(boolean condition, R column, Object... values) {
        return notIn(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .toList());
    }

    /**
     * ignore
     */
    default Children inSoql(R column, String inValue) {
        return inSoql(true, column, inValue);
    }

    /**
     * 字段 IN ( soql语句 )
     * <p>!! soql 注入方式的 in 方法 !!</p>
     * <p>例1: inSoql("id", "1, 2, 3, 4, 5, 6")</p>
     * <p>例2: inSoql("id", "select id from table where id &lt; 3")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param inValue   soql语句
     * @return children
     */
    Children inSoql(boolean condition, R column, String inValue);

    /**
     * ignore
     */
    default Children notInSoql(R column, String inValue) {
        return notInSoql(true, column, inValue);
    }

    /**
     * 字段 NOT IN ( soql语句 )
     * <p>!! soql 注入方式的 not in 方法 !!</p>
     * <p>例1: notInSoql("id", "1, 2, 3, 4, 5, 6")</p>
     * <p>例2: notInSoql("id", "select id from table where id > 3")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param inValue   soql语句 ---> 1,2,3,4,5,6 或者 select id from table where id > 3
     * @return children
     */
    Children notInSoql(boolean condition, R column, String inValue);

    default Children isNull(R column) {
        return isNull(true, column);
    }

    /**
     * If you run a query on a boolean field, null matches FALSE values.
     * The clause WHERE Test_c = null is equivalent to WHERE Test_c = false.
     * <p>例: isNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    Children isNull(boolean condition, R column);

    /**
     * ignore
     */
    default Children isNotNull(R column) {
        return isNotNull(true, column);
    }

    /**
     * If you run a query on a boolean field, not null matches TRUE values.
     * The clause WHERE Test_c != null is equivalent to WHERE Test_c = true.
     * <p>例: isNotNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    Children isNotNull(boolean condition, R column);

    /**
     * ignore
     */
    Children orderByAsc(R column);

    /**
     * ignore
     */
    Children orderByAsc(R... columns);

    /**
     * 排序：ORDER BY 字段, ... ASC
     * <p>例: orderByAsc("id", "name")</p>
     *
     * @param isNullsFirst NULLS FIRST 或 NULLS LAST：将空值记录排序在结果的开头（NULLS FIRST）或末尾（NULLS LAST）。默认情况下，空值会优先排序。
     * @param columns      字段数组
     * @return children
     */

    default Children orderByAsc(boolean isNullsFirst, R... columns) {
        return orderBy(true, isNullsFirst, columns);
    }

    Children orderByDesc(R column);

    /**
     * ignore
     */
    Children orderByDesc(R... columns);

    /**
     * 排序：ORDER BY 字段, ... DESC
     * <p>例: orderByDesc("id", "name")</p>
     *
     * @param isNullsFirst NULLS FIRST 或 NULLS LAST：将空值记录排序在结果的开头（NULLS FIRST）或末尾（NULLS LAST）。默认情况下，空值会优先排序。
     * @param columns      字段数组
     * @return children
     */
    default Children orderByDesc(boolean isNullsFirst, R... columns) {
        return orderBy(false, isNullsFirst, columns);
    }

    /**
     * 排序：ORDER BY 字段, ...
     * <p>例: orderBy(true,false, "id", "name")</p>
     *
     * @param isAsc        是否是 ASC 排序
     * @param isNullsFirst NULLS FIRST 或 NULLS LAST：将空值记录排序在结果的开头（NULLS FIRST）或末尾（NULLS LAST）。默认情况下，空值会后排序。
     * @param columns      字段数组
     * @return children
     */
    Children orderBy(boolean isAsc, boolean isNullsFirst, R... columns);

    /**
     * @param condition    执行条件
     * @param isAsc        是否是 ASC 排序
     * @param isNullsFirst NULLS FIRST 或 NULLS LAST：将空值记录排序在结果的开头（NULLS FIRST）或末尾（NULLS LAST）。默认情况下，空值会优先排序。
     * @param columns      字段数组
     * @return children
     */
    Children orderBy(boolean condition, boolean isAsc, boolean isNullsFirst, R... columns);

    /**
     * ignore
     */
    Children groupBy(R column);

    /**
     * ignore
     */
    Children groupBy(R... columns);

    /**
     * 分组：GROUP BY 字段, ...
     * <p>例: groupBy("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return children
     */
    Children groupBy(boolean condition, R... columns);
}