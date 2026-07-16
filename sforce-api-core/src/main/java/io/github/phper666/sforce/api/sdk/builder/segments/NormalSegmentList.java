package io.github.phper666.sforce.api.sdk.builder.segments;

import io.github.phper666.sforce.api.sdk.builder.LogicOperator;
import io.github.phper666.sforce.api.sdk.builder.interfaces.SoqlSegment;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static io.github.phper666.sforce.api.sdk.builder.SoqlKeyword.WHERE;
import static io.github.phper666.sforce.api.sdk.builder.StringPool.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Setter
@Accessors(chain = true)
public class NormalSegmentList extends ArrayList<SoqlSegment> implements SoqlSegment {
    private boolean isNestedSegments = false;

    public SoqlSegment getLastSegment() {
        return this.get(super.size() - 1);
    }

    public SoqlSegment getFirstSegment() {
        return this.get(0);
    }

    public void add(SoqlSegment... soqlSegment) {
        super.addAll(Arrays.asList(soqlSegment));
    }

    @Override
    public String getSoqlSegment() {
        if (isEmpty()) {
            return EMPTY;
        }
        if (!isNestedSegments) {
            SoqlSegment soqlSegment = getFirstSegment();
            if (soqlSegment instanceof LogicOperator) {
                remove(0);
            }
        }
        final String str = this.stream().map(SoqlSegment::getSoqlSegment).collect(Collectors.joining(SPACE));
        // return isNestedSegments ? (LEFT_BRACKET + str + RIGHT_BRACKET) : SPACE + WHERE.getSoqlSegment() + SPACE + str;
        return isNestedSegments ? str : SPACE + WHERE.getSoqlSegment() + SPACE + str;
    }
}
