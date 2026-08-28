package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.Session;

import com.google.gson.annotations.SerializedName;
import io.github.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;
import io.github.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import io.github.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import io.github.phper666.sforce.api.sdk.internal.BaseApi;
import io.github.phper666.sforce.api.sdk.model.BulkApiCreateJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiJobDetailResponse;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobResponse;
import io.github.phper666.sforce.api.sdk.serialize.JsonSerializer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BulkApi extends BaseApi {
    BulkApi(SdkConfig config, Session session, OkHttpClient okHttpClient, JsonSerializer jsonSerializer, BaseAuthenticator authFlow) {
        super(config, session, okHttpClient, jsonSerializer, authFlow);
    }

    // ──────────────────────────────────────────────
    // Bulk API 2.0
    // ──────────────────────────────────────────────

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public BulkApiJobDetailResponse createBulkApiJob(BulkApiCreateJobRequest request, File fileToUpload, TimeoutSettings timeOutConfig) {
        var url = bulkApiUriBase();
        var rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(request));
        var jobResponseBody = executeGetBody(url, HttpMethod.POST.name(), rb, EMPTY_HEADERS, timeOutConfig);
        var jobDetail = (BulkApiJobDetailResponse) jsonSerializer.fromJson(jobResponseBody, BulkApiJobDetailResponse.class);
        var contentUrl = session.apiEndpoint() + "/" + jobDetail.getContentUrl();
        log.info("upload file: {} to bulk api job: {}", fileToUpload.getAbsolutePath(), contentUrl);
        uploadBulkApiJobRecordFile(contentUrl, fileToUpload, timeOutConfig);
        var markUrl = bulkApiUriBase() + "/" + jobDetail.getId() + "/";
        log.info("mark bulk api job state to UploadComplete: {}", markUrl);
        markBulkApiJobUploadComplete(markUrl, timeOutConfig);
        return jobDetail;
    }

    @SneakyThrows
    String uploadBulkApiJobRecordFile(String url, File file, TimeoutSettings timeOutConfig) {
        RequestBody rb = RequestBody.create(TEXT_CSV_MEDIA, file);
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, TEXT_CSV_MEDIA.type());
        headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        return executeGetBody(url, HttpMethod.PUT.name(), rb, headers, timeOutConfig);
    }

    String markBulkApiJobUploadComplete(String url, TimeoutSettings timeOutConfig) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("state", JobState.UPLOAD_COMPLETE);
        RequestBody rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(body));
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        return executeGetBody(url, HttpMethod.PATCH.name(), rb, headers, timeOutConfig);
    }

    @SuppressWarnings("unchecked")
    public BulkApiJobDetailResponse getBulkApiJob(String bulkApiJobId, TimeoutSettings timeOutConfig) {
        String url = bulkApiUriBase() + "/" + bulkApiJobId + "/";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        try {
            String body = executeGetBody(url, HttpMethod.GET.name(), EMPTY_BODY, headers, timeOutConfig);
            return (BulkApiJobDetailResponse) jsonSerializer.fromJson(body, BulkApiJobDetailResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void downloadBulkApiJobResult(String bulkApiJobId, JobResultType resultType, File dstFile, TimeoutSettings timeOutConfig) {
        String url = bulkApiUriBase() + "/" + bulkApiJobId + resultType.url();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, TEXT_CSV_MEDIA.type());
        try {
            String body = executeGetBody(url, HttpMethod.GET.name(), EMPTY_BODY, headers, timeOutConfig);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile))) {
                writer.write(body);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ──────────────────────────────────────────────
    // Bulk API 2.0 Query
    // ──────────────────────────────────────────────

    /**
     * Create a Bulk API 2.0 query job.
     *
     * @param request        query job request (object + SOQL query)
     * @param timeOutConfig  per-request timeout settings
     * @return query job details
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public BulkApiQueryJobResponse createBulkQueryJob(BulkApiQueryJobRequest request, TimeoutSettings timeOutConfig) {
        var url = bulkQueryApiUriBase();
        var rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(request));
        var body = executeGetBody(url, HttpMethod.POST.name(), rb, EMPTY_HEADERS, timeOutConfig);
        return (BulkApiQueryJobResponse) jsonSerializer.fromJson(body, BulkApiQueryJobResponse.class);
    }

    /**
     * Get the status of a Bulk API 2.0 query job.
     *
     * @param bulkQueryJobId query job id
     * @param timeOutConfig  per-request timeout settings
     * @return query job details
     */
    @SuppressWarnings("unchecked")
    public BulkApiQueryJobResponse getBulkQueryJob(String bulkQueryJobId, TimeoutSettings timeOutConfig) {
        String url = bulkQueryApiUriBase() + "/" + bulkQueryJobId;
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        try {
            String body = executeGetBody(url, HttpMethod.GET.name(), EMPTY_BODY, headers, timeOutConfig);
            return (BulkApiQueryJobResponse) jsonSerializer.fromJson(body, BulkApiQueryJobResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Download the full results of a completed Bulk API 2.0 query job,
     * paging through all result sets via the {@code Sforce-Locator} header.
     * <p>
     * Each page is a separate HTTP request whose body is streamed straight
     * to {@code dstFile} (never buffered fully in memory). The first page is
     * written verbatim including its CSV header row; subsequent pages skip
     * their duplicate header row.
     *
     * @param bulkQueryJobId query job id
     * @param dstFile        destination file for the CSV result (all pages appended)
     * @param maxRecords     max records per page (null = server default); helps avoid timeout on large results
     * @param timeOutConfig  per-request timeout settings
     */
    public void downloadBulkQueryJobResult(String bulkQueryJobId, File dstFile, Integer maxRecords, TimeoutSettings timeOutConfig) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, TEXT_CSV_MEDIA.type());
        String locator = null;
        boolean firstPage = true;
        try {
            while (true) {
                String url = queryResultsUrl(bulkQueryJobId, locator, maxRecords);
                Response resp = execute(url, HttpMethod.GET.name(), EMPTY_BODY, headers, timeOutConfig);
                if (firstPage) {
                    try (InputStream in = resp.body().byteStream();
                         OutputStream out = new FileOutputStream(dstFile)) {
                        in.transferTo(out);
                    }
                    firstPage = false;
                } else {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resp.body().byteStream(), StandardCharsets.UTF_8));
                         OutputStream out = new FileOutputStream(dstFile, true)) {
                        reader.readLine(); // skip duplicate header row
                        char[] buf = new char[8192];
                        int n;
                        while ((n = reader.read(buf)) != -1) {
                            out.write(new String(buf, 0, n).getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
                locator = resp.header("Sforce-Locator");
                resp.close();
                if (locator == null || "null".equals(locator)) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Download the results of a completed Bulk API 2.0 query job using the
     * server default page size.
     *
     * @param bulkQueryJobId query job id
     * @param dstFile        destination file for the CSV result
     * @param timeOutConfig  per-request timeout settings
     */
    public void downloadBulkQueryJobResult(String bulkQueryJobId, File dstFile, TimeoutSettings timeOutConfig) {
        downloadBulkQueryJobResult(bulkQueryJobId, dstFile, null, timeOutConfig);
    }

    private String queryResultsUrl(String jobId, String locator, Integer maxRecords) {
        String url = bulkQueryApiUriBase() + "/" + jobId + "/results";
        String sep = "?";
        if (locator != null) {
            url += sep + "locator=" + URLEncoder.encode(locator, StandardCharsets.UTF_8);
            sep = "&";
        }
        if (maxRecords != null) {
            url += sep + "maxRecords=" + maxRecords;
        }
        return url;
    }

    /**
     * Wait for a query job to reach {@link JobState#JOB_COMPLETE} (or {@link JobState#FAILED}) state.
     *
     * @param jobId          query job id
     * @param pollIntervalMs polling interval in ms (null = default 3000)
     * @param timeoutMs      max total wait in ms (null = default 30 minutes)
     * @param timeOutConfig  per-request timeout settings
     * @return job details when JobComplete
     * @throws IllegalStateException if job fails or times out
     */
    public BulkApiQueryJobResponse waitForJobComplete(String jobId, Long pollIntervalMs, Long timeoutMs, TimeoutSettings timeOutConfig) {
        long interval = pollIntervalMs != null ? pollIntervalMs : 3000;
        long deadline = System.currentTimeMillis() + (timeoutMs != null ? timeoutMs : 30 * 60 * 1000);
        while (System.currentTimeMillis() < deadline) {
            BulkApiQueryJobResponse status = getBulkQueryJob(jobId, timeOutConfig);
            if (status.getState() == BulkApi.JobState.JOB_COMPLETE) {
                return status;
            }
            if (status.getState() == BulkApi.JobState.FAILED) {
                throw new IllegalStateException("Bulk query job failed: " + jobId + " state=" + status.getState());
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for bulk query job: " + jobId, e);
            }
        }
        throw new IllegalStateException("Timed out waiting for bulk query job: " + jobId);
    }

    /**
     * Run multiple query jobs in parallel and download each result to a file.
     * <p>
     * Jobs are created serially (to avoid token-refresh races on the shared
     * session); waiting for completion and downloading run on a fixed thread
     * pool. Result files are named {@code query-<n>.csv} inside {@code dstDir}
     * (created if missing), mapped back by query string.
     *
     * @param queries       list of SOQL queries
     * @param objectType    object type for each job (all same)
     * @param dstDir        directory to write result CSV files
     * @param concurrency   max parallel jobs (null = default 4)
     * @param maxRecords    max records per result page (null = server default)
     * @param timeOutConfig per-request timeout settings
     * @return Map&lt;String, File&gt; query → downloaded result file
     */
    public Map<String, File> runQueryJobs(List<String> queries, String objectType, File dstDir, Integer concurrency, Integer maxRecords, TimeoutSettings timeOutConfig) {
        if (dstDir != null && !dstDir.exists()) {
            dstDir.mkdirs();
        }
        int poolSize = concurrency != null ? concurrency : 4;
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<String> jobIds = new ArrayList<>();
        for (String query : queries) {
            BulkApiQueryJobRequest req = new BulkApiQueryJobRequest()
                    .setObject(objectType)
                    .setQuery(query);
            jobIds.add(createBulkQueryJob(req, timeOutConfig).getId());
        }
        Map<String, File> results = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < queries.size(); i++) {
            String query = queries.get(i);
            String jobId = jobIds.get(i);
            int idx = i;
            futures.add(executor.submit(() -> {
                waitForJobComplete(jobId, null, null, timeOutConfig);
                File dstFile = new File(dstDir, "query-" + (idx + 1) + ".csv");
                downloadBulkQueryJobResult(jobId, dstFile, maxRecords, timeOutConfig);
                results.put(query, dstFile);
            }));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            for (Future<?> f : futures) {
                f.get(); // propagate task failures
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while running bulk query jobs", e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
        return results;
    }

    /**
     * Delete a Bulk API 2.0 query job.
     *
     * @param bulkQueryJobId query job id
     * @param timeOutConfig  per-request timeout settings
     */
    public void deleteBulkQueryJob(String bulkQueryJobId, TimeoutSettings timeOutConfig) {
        String url = bulkQueryApiUriBase() + "/" + bulkQueryJobId;
        try {
            executeGetBody(url, HttpMethod.DELETE.name(), EMPTY_BODY, EMPTY_HEADERS, timeOutConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public enum ColumnDelimiter {
        BACKQUOTE, CARET, COMMA, PIPE, SEMICOLON, TAB
    }

    public enum LineEnding {
        LF, CRLF
    }

    public enum JobOperation {
        @SerializedName("insert") INSERT,
        @SerializedName("delete") DELETE,
        @SerializedName("hardDelete") HARD_DELETE,
        @SerializedName("update") UPDATE,
        @SerializedName("upsert") UPSERT,
        @SerializedName("query") QUERY,
        /**
         * Returns records that have been deleted (via merge/delete) and archived
         * Task/Event records, in addition to current active data.
         */
        @SerializedName("queryAll") QUERY_ALL
    }

    public enum JobResultType {
        SUCCESSFUL_RESULT("/successfulResults"),
        FAILED_RESULT("/failedResults"),
        UNPROCESSED_RESULT("/unprocessedRecords");

        private final String url;
        JobResultType(String url) { this.url = url; }
        public String url() { return url; }
    }

    public enum JobState {
        @SerializedName("Open") OPEN,
        @SerializedName("UploadComplete") UPLOAD_COMPLETE,
        @SerializedName("Aborted") ABORTED,
        @SerializedName("JobComplete") JOB_COMPLETE,
        @SerializedName("Failed") FAILED
    }
}
