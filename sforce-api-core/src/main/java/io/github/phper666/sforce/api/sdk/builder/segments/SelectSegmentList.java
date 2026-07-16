package io.github.phper666.sforce.api.sdk.builder.segments;

import io.github.phper666.sforce.api.sdk.builder.interfaces.SoqlSegment;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static io.github.phper666.sforce.api.sdk.builder.SoqlKeyword.FROM;
import static io.github.phper666.sforce.api.sdk.builder.SoqlKeyword.SELECT;
import static io.github.phper666.sforce.api.sdk.builder.StringPool.COMMA;
import static io.github.phper666.sforce.api.sdk.builder.StringPool.EMPTY;
import static io.github.phper666.sforce.api.sdk.builder.utils.SoqlUtil.getFields;
import static io.github.phper666.sforce.api.sdk.builder.utils.SoqlUtil.getObjectName;
import static org.apache.commons.lang3.StringUtils.SPACE;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Setter
public class SelectSegmentList extends ArrayList<SoqlSegment> implements SoqlSegment {
    private boolean isNestedSegments = false;
    private String objectName;

    public void initiate(Class<?> entityClass) {
        Validate.notNull(entityClass, "please set the entity class for query");
        this.objectName = getObjectName(entityClass);
        var selectedFields = getFields(entityClass).stream().map(column -> (SoqlSegment) () -> column).toList();
        this.addAll(selectedFields);
    }

    @Override
    public String getSoqlSegment() {
        if (isNestedSegments && isEmpty()) {
            return EMPTY;
        }
        Validate.isTrue(StringUtils.isNotBlank(objectName), "please set the entity class for query");
        Validate.notEmpty(this, "please set the select fields for query");
        String selectedFields = this.stream().map(SoqlSegment::getSoqlSegment).collect(Collectors.joining(COMMA, SPACE, SPACE));
        return SELECT.getSoqlSegment() + selectedFields + FROM.getSoqlSegment() + SPACE + objectName;
    }
}
