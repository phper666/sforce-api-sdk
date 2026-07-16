package io.github.phper666.sforce.api.sdk.builder.interfaces;

import java.io.Serializable;
import java.util.function.Consumer;
/**
 * 查询条件封装
 * <p>嵌套</p>
 * <p>泛型 Param 是具体需要运行函数的类(也是 builder 的子类)</p>
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public interface Nested<Param, Children> extends Serializable {
    default Children and() {
        return and(true);
    }

    Children and(boolean condition);

    /**
     * ignore
     */
    default Children and(Consumer<Param> consumer) {
        return and(true, consumer);
    }

    /**
     * AND 嵌套
     * <p>
     * 例: and(i -> i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children and(boolean condition, Consumer<Param> consumer);

    default Children or() {
        return or(true);
    }

    Children or(boolean condition);

    /**
     * ignore
     */
    default Children or(Consumer<Param> consumer) {
        return or(true, consumer);
    }

    /**
     * OR 嵌套
     * <p>
     * 例: or(i -> i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children or(boolean condition, Consumer<Param> consumer);

    default Children not() {
        return not(true);
    }

    Children not(boolean condition);

    /**
     * ignore
     */
    default Children not(Consumer<Param> consumer) {
        return not(true, consumer);
    }

    /**
     * not嵌套
     * <p>
     * 例: not(i -> i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children not(boolean condition, Consumer<Param> consumer);
}

