package io.github.phper666.sforce.api.sdk.model;
import io.github.phper666.sforce.api.sdk.BulkApi;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Bulk api 2.0
 * <p>
 * see <a href="https://developer.salesforce.com/docs/atlas.en-us.api_asynch.meta/api_asynch/create_job.htm">create a job</a>
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Data
@Accessors(chain = true)
public class BulkApiCreateJobRequest {
    private String object;
    private BulkApi.JobOperation operation;
    private BulkApi.LineEnding lineEnding = BulkApi.LineEnding.LF;
    private BulkApi.ColumnDelimiter columnDelimiter = BulkApi.ColumnDelimiter.COMMA;
    private String externalIdFieldName;
}