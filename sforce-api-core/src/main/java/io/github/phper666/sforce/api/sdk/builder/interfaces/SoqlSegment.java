package io.github.phper666.sforce.api.sdk.builder.interfaces;

/**
 * Interface for a single segment in a SOQL query.
 *
 * @author Yuzhao.Li
 */
@FunctionalInterface
public interface SoqlSegment {
    /**
     * SOQL 片段
     */
    String getSoqlSegment();
}
