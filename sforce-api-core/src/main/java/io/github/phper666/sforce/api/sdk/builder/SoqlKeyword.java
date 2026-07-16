package io.github.phper666.sforce.api.sdk.builder;

import io.github.phper666.sforce.api.sdk.builder.interfaces.SoqlSegment;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum of SOQL keywords and operators.
 *
 * @author Yuzhao.Li
 */
@Getter
@AllArgsConstructor
public enum SoqlKeyword implements SoqlSegment {
    SELECT("SELECT"),
    FROM("FROM"),
    WHERE("WHERE"),
    LIMIT("LIMIT"),
    OFFSET("OFFSET"),
    FIELDS_ALL("Fields(all)"),
    COUNT("COUNT()"),
    COUNT_FIELD("COUNT(%s)"),
    EQ("="),
    NEQ("!="),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),
    LIKE("LIKE"),
    IN("IN"),
    NOT_IN("NOT IN"),
    INCLUDES("INCLUDES"),
    EXCLUDES("EXCLUDES"),
    GROUP_BY("GROUP BY"),
    ORDER_BY("ORDER BY"),
    ASC("ASC"),
    DESC("DESC"),
    NULLS_FIRST("NULLS FIRST"),
    NULLS_LAST("NULLS LAST");
    private final String keyword;

    @Override
    public String getSoqlSegment() {
        return keyword;
    }
}
