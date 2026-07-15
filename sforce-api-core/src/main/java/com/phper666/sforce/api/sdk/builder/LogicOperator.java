package com.phper666.sforce.api.sdk.builder;

import com.phper666.sforce.api.sdk.builder.interfaces.SoqlSegment;

/**
 * @author Yuzhao.LI
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
