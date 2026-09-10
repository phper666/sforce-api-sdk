package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.Session;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import io.github.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;
import io.github.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import io.github.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import io.github.phper666.sforce.api.sdk.internal.BaseApi;
import io.github.phper666.sforce.api.sdk.model.BulkApiCreateJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiJobDetailResponse;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobResponse;
import io.github.phper666.sforce.api.sdk.model.BulkApiResultPagesResponse;
import io.github.phper666.sforce.api.sdk.serialize.JsonSerializer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import java.io.Reader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
        // Bulk 2.0 query job 不接受 object 字段（SOQL 已含对象），序列化后剥除
        JsonObject payload = JsonParser.parseString(jsonSerializer.toJson(request)).getAsJsonObject();
        payload.remove("object");
        var rb = RequestBody.create(JSON_MEDIA, payload.toString());
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
                if (locator == null || locator.isEmpty() || "null".equals(locator)) {
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

    // ──────────────────────────────────────────────
    // Bulk API 2.0 Query — resultPages 并行下载（API v58.0+）
    // ──────────────────────────────────────────────

    /** 官方每轮最多返回 5 个 resultUrl */
    private static final int DEFAULT_RESULT_PAGES_CONCURRENCY = 5;
    /** 单段下载失败重试次数（尝试 3 次）；locator 幂等所以重试安全 */
    private static final int SEGMENT_DOWNLOAD_ATTEMPTS = 3;
    /** 防死循环：done 一直 false 且 nextUrl 不前进时的最大轮数 */
    private static final int MAX_RESULT_PAGES_ROUNDS = 10000;

    private String toAbsolute(String url) {
        if (url == null || url.isEmpty() || url.startsWith("http")) {
            return url;
        }
        if (url.startsWith("/services/data/")) {
            return session.apiEndpoint() + url;
        }
        // 实测（v62）chunk 链接为版本-less 相对路径（/jobs/query/...），补 /services/data/{version}
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + url;
    }

    /**
     * 循环 GET /resultPages 收集所有 resultUrl（已拼成绝对 URL）。
     * <p>
     * 官方字段名不一致（表格 nextRecordUrl / 示例 nextRecordsUrl），两个都解析取非空的。
     */
    private List<String> collectResultUrls(String jobId, TimeoutSettings timeOutConfig) {
        List<String> urls = new ArrayList<>();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        String nextUrl = bulkQueryApiUriBase() + "/" + jobId + "/resultPages";
        for (int round = 0; round < MAX_RESULT_PAGES_ROUNDS; round++) {
            String body;
            try {
                body = executeGetBody(nextUrl, HttpMethod.GET.name(), EMPTY_BODY, headers, timeOutConfig);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        BulkApiResultPagesResponse resp = (BulkApiResultPagesResponse) jsonSerializer.fromJson(body, BulkApiResultPagesResponse.class);
        // 兼容两种响应形态：官方文档（resultPages/resultUrl）与实测 v62（resultChunks/resultLink）
        resp.allResultUrls().forEach(u -> urls.add(toAbsolute(u)));
        if (Boolean.TRUE.equals(resp.getDone())) {
            break;
        }
        String next = resp.nextPageUrl();
        if (next == null || next.isEmpty()) {
            break;
        }
        nextUrl = toAbsolute(next);
        }
        return urls;
    }

    /**
     * Download one result segment (CSV) with retry. Locator is idempotent so
     * retrying a failed segment download is safe.
     */
    private String downloadResultSegment(String url, TimeoutSettings timeOutConfig) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, TEXT_CSV_MEDIA.type());
        RuntimeException last = null;
        for (int attempt = 0; attempt < SEGMENT_DOWNLOAD_ATTEMPTS; attempt++) {
            try {
                Response resp = execute(url, HttpMethod.GET.name(), EMPTY_BODY, headers, timeOutConfig);
                try {
                    return resp.body() == null ? "" : resp.body().string();
                } finally {
                    resp.close();
                }
            } catch (IOException e) {
                last = new RuntimeException(e);
            } catch (RuntimeException e) {
                last = e;
            }
        }
        throw last;
    }

    /** 首行（去 \r），无换行时返回整段 */
    private static String firstLine(String segment) {
        int nl = segment.indexOf('\n');
        String line = nl < 0 ? segment : segment.substring(0, nl);
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }

    /** 后续段去重表头：首行与第一段表头相同则跳过，否则保留（容错：不假设每段都带表头） */
    private static String stripDuplicateHeader(String segment, String headerLine) {
        int nl = segment.indexOf('\n');
        if (nl < 0) {
            return segment.equals(headerLine) ? "" : segment;
        }
        String first = segment.substring(0, nl);
        if (first.endsWith("\r")) {
            first = first.substring(0, first.length() - 1);
        }
        return first.equals(headerLine) ? segment.substring(nl + 1) : segment;
    }

    private static int resolveConcurrency(Integer concurrency) {
        return concurrency != null && concurrency > 0 ? concurrency : DEFAULT_RESULT_PAGES_CONCURRENCY;
    }

    /**
     * Download query job results using the parallel resultPages API (v58.0+).
     * <p>
     * Downloads up to {@code concurrency} result sets concurrently (sliding
     * window — at most {@code concurrency} segments are in flight/in memory at
     * any time), writing pages to {@code dstFile} in order: the first segment
     * keeps its CSV header, later segments skip a duplicate header row (a
     * segment without a header is kept verbatim).
     *
     * @param jobId        completed query job id
     * @param dstFile      destination file (parent dirs created if missing)
     * @param concurrency  max parallel segment downloads (null = default 5)
     * @param timeOutConfig per-request timeout settings
     */
    public void downloadBulkQueryJobResultParallel(String jobId, File dstFile, Integer concurrency, TimeoutSettings timeOutConfig) {
        List<String> urls = collectResultUrls(jobId, timeOutConfig);
        if (dstFile.getParentFile() != null) {
            dstFile.getParentFile().mkdirs();
        }
        int poolSize = resolveConcurrency(concurrency);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        try {
            // 滑动窗口：最多 poolSize 段同时在内存/网络中，按序写文件
            List<Future<String>> window = new ArrayList<>();
            int next = 0;
            boolean firstSegment = true;
            String headerLine = null;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile))) {
                while (next < urls.size() || !window.isEmpty()) {
                    while (window.size() < poolSize && next < urls.size()) {
                        String url = urls.get(next++);
                        window.add(executor.submit(() -> downloadResultSegment(url, timeOutConfig)));
                    }
                    String segment = window.remove(0).get();
                    if (firstSegment) {
                        headerLine = firstLine(segment);
                        writer.write(segment);
                        firstSegment = false;
                    } else {
                        writer.write(stripDuplicateHeader(segment, headerLine));
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while downloading bulk query results in parallel: " + jobId, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Same as {@link #downloadBulkQueryJobResultParallel} but delivers parsed
     * pages to a consumer in order, instead of writing a file. Memory-bounded
     * via sliding-window concurrency.
     *
     * @param jobId        completed query job id
     * @param concurrency  max parallel segment downloads (null = default 5)
     * @param consumer     page consumer (pages delivered in resultPages order)
     * @param timeOutConfig per-request timeout settings
     */
    public void forEachResultPageParallel(String jobId, Integer concurrency,
            Consumer<List<JsonObject>> consumer, TimeoutSettings timeOutConfig) {
        Objects.requireNonNull(consumer, "consumer must not be null");
        List<String> urls = collectResultUrls(jobId, timeOutConfig);
        int poolSize = resolveConcurrency(concurrency);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        try {
            List<Future<List<JsonObject>>> window = new ArrayList<>();
            int next = 0;
            while (next < urls.size() || !window.isEmpty()) {
                while (window.size() < poolSize && next < urls.size()) {
                    String url = urls.get(next++);
                    window.add(executor.submit(() -> parseCsvToJson(downloadResultSegment(url, timeOutConfig))));
                }
                consumer.accept(window.remove(0).get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while consuming bulk query results in parallel: " + jobId, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Convenience: stream results row by row (page-level fetch internally).
     * Simple wrapper over {@link #forEachResultPage(String, Integer, Consumer, TimeoutSettings)}
     * — no file, no full materialization.
     *
     * @param jobId        completed query job id
     * @param maxRecords   max records per page (null = server default)
     * @param rowConsumer  invoked once per record
     * @param timeOutConfig per-request timeout settings
     */
    public void forEachResultRow(String jobId, Integer maxRecords,
            Consumer<JsonObject> rowConsumer, TimeoutSettings timeOutConfig) {
        Objects.requireNonNull(rowConsumer, "rowConsumer must not be null");
        forEachResultPage(jobId, maxRecords, page -> {
            for (JsonObject row : page) {
                rowConsumer.accept(row);
            }
        }, timeOutConfig);
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
        long interval = pollIntervalMs != null ? pollIntervalMs : 5000;
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

    /**
     * Fetch all result pages of a completed Bulk API 2.0 query job, invoking the
     * consumer for each page in order. Each page is parsed to a list of records
     * (CSV header row mapped to field names, all values as strings).
     * <p>
     * The job is NOT deleted on failure/interruption — it is preserved so the
     * caller can retry or inspect it. When {@code deleteOnComplete} is true the
     * job is deleted ONLY after every page has been fully consumed and the
     * received row count matches the job's {@code numberRecordsProcessed}.
     *
     * @param jobId            query job id
     * @param maxRecords       max records per page (null = server default 50,000); only splits pages smaller
     * @param pollIntervalMs   polling interval while waiting for job completion (null = default 5000)
     * @param timeoutMs        max total wait for job completion (null = default 30 minutes)
     * @param consumer         page consumer (one page at a time, caller may discard after return)
     * @param timeOutConfig    per-request timeout settings
     * @param deleteOnComplete delete the job after full consumption (opt-in; default false)
     * @throws IllegalStateException if the row count does not match the job's processed count
     */
    public void forEachResultPage(String jobId, Integer maxRecords, Long pollIntervalMs, Long timeoutMs,
            Consumer<List<JsonObject>> consumer, TimeoutSettings timeOutConfig, boolean deleteOnComplete) {
        Objects.requireNonNull(consumer, "consumer must not be null");
        BulkApiQueryJobResponse job = waitForJobComplete(jobId, pollIntervalMs, timeoutMs, timeOutConfig);
        Integer expected = job.getNumberRecordsProcessed();
        int received = 0;
        String locator = null;
        try {
            while (true) {
                Map<String, String> headers = new HashMap<>();
                headers.put(HttpHeaders.ACCEPT, TEXT_CSV_MEDIA.type());
                Response resp;
                try {
                    resp = execute(
                            queryResultsUrl(jobId, locator, maxRecords),
                            HttpMethod.GET.name(), EMPTY_BODY, headers, timeOutConfig);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String body;
                String nextLocator;
                try {
                    body = resp.body() == null ? "" : resp.body().string();
                    nextLocator = resp.header("Sforce-Locator");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    resp.close();
                }
                List<JsonObject> page = parseCsvToJson(body);
                received += page.size();
                consumer.accept(page);
                locator = nextLocator;
                if (locator == null || locator.isEmpty() || "null".equals(locator)) {
                    break;
                }
            }
            if (expected != null && received != expected) {
                throw new IllegalStateException(
                        "Bulk query result row count mismatch: expected " + expected
                                + " but received " + received + " (job " + jobId + ")");
            }
            if (deleteOnComplete) {
                deleteBulkQueryJob(jobId, timeOutConfig);
            }
        } catch (RuntimeException e) {
            // 失败/中断：不删 job，保留供重试或检查
            throw e;
        }
    }

    /**
     * Convenience overload: default polling, no auto-delete.
     */
    public void forEachResultPage(String jobId, Integer maxRecords,
            Consumer<List<JsonObject>> consumer, TimeoutSettings timeOutConfig) {
        forEachResultPage(jobId, maxRecords, null, null, consumer, timeOutConfig, false);
    }

    /**
     * Lazy iterator over result pages of a completed Bulk API 2.0 query job.
     * <p>
     * Pages are fetched on demand (one HTTP request per page) and held in memory
     * one at a time. The job is NOT deleted implicitly on close: use
     * {@link #deleteBulkQueryJob(String, TimeoutSettings)} explicitly, or pass
     * {@code deleteOnComplete=true} to the factory to auto-delete ONLY after all
     * pages were fully consumed (early {@code break} keeps the job).
     */
    public class QueryResultIterator implements Iterator<List<JsonObject>>, AutoCloseable {
        private final String jobId;
        private final Integer maxRecords;
        private final Long pollIntervalMs;
        private final Long timeoutMs;
        private final TimeoutSettings timeOutConfig;
        private final boolean deleteOnComplete;

        private boolean jobWaited;
        private Integer expectedRows;
        private int receivedRows;
        private String nextLocator;
        private List<JsonObject> bufferedPage;
        private boolean exhausted;
        private boolean fullyConsumed;
        private boolean closed;

        private QueryResultIterator(String jobId, Integer maxRecords, Long pollIntervalMs, Long timeoutMs,
                TimeoutSettings timeOutConfig, boolean deleteOnComplete) {
            this.jobId = jobId;
            this.maxRecords = maxRecords;
            this.pollIntervalMs = pollIntervalMs;
            this.timeoutMs = timeoutMs;
            this.timeOutConfig = timeOutConfig;
            this.deleteOnComplete = deleteOnComplete;
        }

        private void ensureJobReady() {
            if (jobWaited) {
                return;
            }
            expectedRows = waitForJobComplete(jobId, pollIntervalMs, timeoutMs, timeOutConfig)
                    .getNumberRecordsProcessed();
            jobWaited = true;
        }

        @Override
        public boolean hasNext() {
            ensureJobReady();
            if (bufferedPage == null && !exhausted) {
                bufferedPage = fetchNextPage();
            }
            return bufferedPage != null;
        }

        @Override
        public List<JsonObject> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("no more bulk query result pages");
            }
            List<JsonObject> page = bufferedPage;
            bufferedPage = null;
            return page;
        }

        private List<JsonObject> fetchNextPage() {
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.ACCEPT, TEXT_CSV_MEDIA.type());
            Response resp;
            try {
                resp = execute(
                        queryResultsUrl(jobId, nextLocator, maxRecords),
                        HttpMethod.GET.name(), EMPTY_BODY, headers, timeOutConfig);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String body;
            String locatorHeader;
            try {
                body = resp.body() == null ? "" : resp.body().string();
                locatorHeader = resp.header("Sforce-Locator");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                resp.close();
            }
            List<JsonObject> page = parseCsvToJson(body);
            receivedRows += page.size();
            nextLocator = locatorHeader;
            if (nextLocator == null || nextLocator.isEmpty() || "null".equals(nextLocator)) {
                exhausted = true;
                if (expectedRows != null && receivedRows != expectedRows) {
                    throw new IllegalStateException(
                            "Bulk query result row count mismatch: expected " + expectedRows
                                    + " but received " + receivedRows + " (job " + jobId + ")");
                }
                fullyConsumed = true;
            }
            return page;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (deleteOnComplete && fullyConsumed) {
                deleteBulkQueryJob(jobId, timeOutConfig);
            }
            // 提前 break / 未完整消费 / 异常：不删 job
        }
    }

    /**
     * Create a lazy page iterator for a Bulk API 2.0 query job.
     */
    public QueryResultIterator queryResultIterator(String jobId, Integer maxRecords, Long pollIntervalMs,
            Long timeoutMs, TimeoutSettings timeOutConfig, boolean deleteOnComplete) {
        return new QueryResultIterator(jobId, maxRecords, pollIntervalMs, timeoutMs, timeOutConfig, deleteOnComplete);
    }

    /**
     * Convenience overload: default polling, no auto-delete.
     */
    public QueryResultIterator queryResultIterator(String jobId, Integer maxRecords, TimeoutSettings timeOutConfig) {
        return queryResultIterator(jobId, maxRecords, null, null, timeOutConfig, false);
    }

    private static List<JsonObject> parseCsvToJson(String csv) {
        List<JsonObject> records = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return records;
        }
        // Salesforce 结果可能含 NUL 字节（\u0000，长文本字段实测坑，见 airbyte#8300），
        // 会破坏 CSV 解析，解析前统一剥除
        if (csv.indexOf('\u0000') >= 0) {
            csv = csv.replace("\u0000", "");
        }
        try (Reader reader = new StringReader(csv);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {
            for (CSVRecord row : parser) {
                JsonObject obj = new JsonObject();
                row.toMap().forEach(obj::addProperty);
                records.add(obj);
            }
            return records;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse bulk query CSV results", e);
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
        @SerializedName("InProgress") IN_PROGRESS,
        @SerializedName("Aborted") ABORTED,
        @SerializedName("JobComplete") JOB_COMPLETE,
        @SerializedName("Failed") FAILED
    }
}
