package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of {@code GET /jobs/query/{jobId}/resultPages} (Bulk API 2.0, API v58.0+).
 * <p>
 * The real-world payload differs from the published docs — both shapes are
 * deserialized here:
 * <ul>
 *   <li>docs: {@code resultPages[]} with {@code resultUrl}, and a next-page URL
 *       named {@code nextRecordUrl} (table) or {@code nextRecordsUrl} (example)</li>
 *   <li>live API (observed v62): {@code resultChunks[]} with {@code resultLink}</li>
 * </ul>
 * Chunk links from the live API are version-less (e.g.
 * {@code /jobs/query/{id}/results?locator=...}) and are resolved against the
 * configured API version by the caller.
 *
 * @author Yuzhao.Li
 */
@Data
@Accessors(chain = true)
public class BulkApiResultPagesResponse {
    private List<ResultPage> resultPages;
    private List<ResultChunk> resultChunks;
    private String nextRecordUrl;    // 官方表格字段名
    private String nextRecordsUrl;   // 官方示例字段名（两个都解析）
    private Boolean done;

    /**
     * All result URLs from either response shape, in order: {@code resultPages}
     * entries then {@code resultChunks} entries.
     */
    public List<String> allResultUrls() {
        List<String> urls = new ArrayList<>();
        if (resultPages != null) {
            for (ResultPage page : resultPages) {
                if (page != null && page.getResultUrl() != null && !page.getResultUrl().isEmpty()) {
                    urls.add(page.getResultUrl());
                }
            }
        }
        if (resultChunks != null) {
            for (ResultChunk chunk : resultChunks) {
                if (chunk != null && chunk.getResultLink() != null && !chunk.getResultLink().isEmpty()) {
                    urls.add(chunk.getResultLink());
                }
            }
        }
        return urls;
    }

    /** Next-page URL from either documented field name (null when neither is set). */
    public String nextPageUrl() {
        if (nextRecordsUrl != null && !nextRecordsUrl.isEmpty()) {
            return nextRecordsUrl;
        }
        return (nextRecordUrl != null && !nextRecordUrl.isEmpty()) ? nextRecordUrl : null;
    }

    @Data
    public static class ResultPage {
        private String resultUrl;
    }

    @Data
    public static class ResultChunk {
        private String resultLink;
    }
}