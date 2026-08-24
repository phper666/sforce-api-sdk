package io.github.phper666.sforce.api.sdk.model;

import io.github.phper666.sforce.api.sdk.BulkApi;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Response containing details of a Bulk API 2.0 query job.
 *
 * @author Yuzhao.Li
 */
@Data
@Accessors(chain = true)
public class BulkApiQueryJobResponse {
    private String apiVersion;
    private String concurrencyMode;
    private String contentType;
    private String createdById;
    private String createdDate;
    private String id;
    private String jobType;
    private String object;
    private BulkApi.JobOperation operation;
    private String query;
    private BulkApi.JobState state;
    private String systemModstamp;
    private Integer numberRecordsProcessed;
    private Integer retries;
    private Integer totalProcessingTime;
}