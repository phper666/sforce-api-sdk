package com.phper666.sforce.api.sdk.model;
import com.phper666.sforce.api.sdk.BulkApi;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Data
@Accessors(chain = true)
public class BulkApiJobDetailResponse {
    private String apiVersion;
    private String assignmentRuleId;
    private BulkApi.ColumnDelimiter columnDelimiter;
    private String concurrencyMode;
    private String contentType;
    private String contentUrl;
    private String createdById;
    private String createdDate;
    private String externalIdFieldName;
    private String id;
    private String jobType;
    private BulkApi.LineEnding lineEnding;
    private String object;
    private BulkApi.JobOperation operation;
    private BulkApi.JobState state;
    private String systemModstamp;
}