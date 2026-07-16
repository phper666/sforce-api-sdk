package io.github.phper666.sforce.api.sdk.builder.interfaces;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Serializable Function
 * @param <T>
 * @param <R>
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
