package io.github.phper666.sforce.api.sdk.builder;

import io.github.phper666.sforce.api.sdk.builder.interfaces.SoqlSegment;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
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
