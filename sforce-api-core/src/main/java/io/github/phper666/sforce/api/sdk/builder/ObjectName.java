package io.github.phper666.sforce.api.sdk.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying Salesforce object name on a class.
 *
 * @author Yuzhao.Li
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface ObjectName {
    String value() default "";
}
