package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * Response wrapper for paginated SOQL queries.
 *
 * @author Yuzhao.Li
 */
@Data
public class PageQueryResponse<T> {
    private Integer totalSize;
    private Boolean done;
    private List<T> records;
    private String nextRecordsUrl;
}
