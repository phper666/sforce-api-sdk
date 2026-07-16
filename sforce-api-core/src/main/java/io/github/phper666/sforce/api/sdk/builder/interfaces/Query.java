package io.github.phper666.sforce.api.sdk.builder.interfaces;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * 查询字段封装
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public interface Query<Children, T, R> extends Serializable {

    /**
     * 设置查询字段,多次调用以最后一次字段为查询字段
     *
     * @param columns 字段数组
     * @return children
     */
    @SuppressWarnings("unchecked")
    Children select(R... columns);

    /**
     * 设置查询字段和字段别名
     *
     * @param column   字段
     * @param alias    别名
     * @param isAppend 是否添加的查询字段集合,false->只查询当前字段,多次调用以最后一次字段为查询字段
     */
    Children select(R column, String alias, boolean isAppend);

    Children select(R column, boolean isAppend);

    /**
     * 设置查询的对象,适用结合无参构造器
     *
     * @param entityClass 查询对象
     */
    Children select(Class<T> entityClass);

    /**
     * 过滤查询字段信息
     * 例1: 只要 java 字段名以 "test" 开头的 -> select(i -> i.getProperty().startsWith("test"))
     * 例2: 只要 java 字段名为 "name" 的 -> select(i -> i.getProperty().equals("name"))
     * 例2: 查除了 java 字段名为 "name" 的所有字段 -> select(i -> !i.getProperty().equals("name"))
     * @param predicate 过滤方式
     */
    Children select(Class<T> entityClass, Predicate<String> predicate);

    /**
     * 设置不查询的字段数组
     *
     * @param columns 字段数组
     */
    @SuppressWarnings("unchecked")
    Children unselect(R... columns);

    /**
     * 设置不查询的字段
     *
     * @param condition 执行条件
     * @param column    字段
     */
    Children unselect(boolean condition, R column);

    /**
     * 设置查询count-> COUNT()
     * 默认只已COUNT()为查询字段
     */
    default Children selectCount() {
        return selectCount(false);
    }

    /**
     * 设置查询count-> COUNT()
     *
     * @param isAppend 是否添加到查询字段集合中
     */
    Children selectCount(boolean isAppend);


    default Children selectCount(R column) {
        return selectCount(column, false);
    }

    /**
     * 设置查询count字段 COUNT(column)
     *
     * @param column   count的字段
     * @param isAppend 是否添加到查询字段集合中
     */
    Children selectCount(R column, boolean isAppend);

    default Children selectCount(R column, String alias) {
        return selectCount(column, alias, false);
    }

    /**
     * 设置查询count字段和别名 COUNT(column) alias
     *
     * @param column   count的字段
     * @param alias    别名
     * @param isAppend 是否添加到查询字段集合中
     */
    Children selectCount(R column, String alias, boolean isAppend);

    /**
     * 设置查询字段为Fields(all)
     */
    Children selectFieldAll();
}
