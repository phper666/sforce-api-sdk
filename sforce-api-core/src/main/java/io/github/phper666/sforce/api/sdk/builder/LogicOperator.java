package io.github.phper666.sforce.api.sdk.builder;

import io.github.phper666.sforce.api.sdk.builder.interfaces.SoqlSegment;

/**
 * Enum representing SOQL logical operators.
 *
 * @author Yuzhao.Li
 */
public enum LogicOperator implements SoqlSegment {
    AND,
    OR,
    NOT;

    @Override
    public String getSoqlSegment() {
        return this.name();
    }
}
