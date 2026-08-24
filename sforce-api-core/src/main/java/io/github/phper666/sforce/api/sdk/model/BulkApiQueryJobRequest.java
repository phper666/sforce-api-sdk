package io.github.phper666.sforce.api.sdk.model;

import io.github.phper666.sforce.api.sdk.BulkApi;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request for creating a Bulk API 2.0 query job.
 *
 * @author Yuzhao.Li
 */
@Data
@Accessors(chain = true)
public class BulkApiQueryJobRequest {
    private String object;
    private String query;
    private BulkApi.JobOperation operation = BulkApi.JobOperation.QUERY;
}