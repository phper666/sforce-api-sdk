package io.github.phper666.sforce.api.sdk.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks types requiring custom namespace serialization.
 *
 * @author Yuzhao.Li
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AppendCustomNamespace {
}
