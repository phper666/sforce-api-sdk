package io.github.phper666.sforce.api.sdk.builder.segments;

import io.github.phper666.sforce.api.sdk.builder.interfaces.SoqlSegment;

import java.util.ArrayList;
import java.util.Arrays;

import static io.github.phper666.sforce.api.sdk.builder.StringPool.EMPTY;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.SPACE;

/**
 * Segment list for LIMIT and OFFSET clauses.
 *
 * @author Yuzhao.Li
 */
public class PageSegmentList extends ArrayList<SoqlSegment> implements SoqlSegment {
    public void add(SoqlSegment... segments) {
        super.addAll(Arrays.asList(segments));
    }

    @Override
    public String getSoqlSegment() {
        if (isEmpty()) {
            return EMPTY;
        }
        return this.stream().map(SoqlSegment::getSoqlSegment).collect(joining(SPACE, SPACE, EMPTY));
    }
}
