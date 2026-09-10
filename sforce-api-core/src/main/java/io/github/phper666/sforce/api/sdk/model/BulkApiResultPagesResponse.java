package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Response of {@code GET /jobs/query/{jobId}/resultPages} (Bulk API 2.0, API v58.0+).
 * <p>
 * The official docs use two different field names for the next-page URL
 * ({@code nextRecordUrl} in the response table, {@code nextRecordsUrl} in the
 * example payload) — both are deserialized and the SDK picks whichever is set.
 *
 * @author Yuzhao.Li
 */
@Data
@Accessors(chain = true)
public class BulkApiResultPagesResponse {
    private List<ResultPage> resultPages;
    private String nextRecordUrl;    // 官方表格字段名
    private String nextRecordsUrl;   // 官方示例字段名（两个都解析）
    private Boolean done;

    @Data
    public static class ResultPage {
        private String resultUrl;
    }
}
