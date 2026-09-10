package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.model.BulkApiCreateJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiJobDetailResponse;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobResponse;
import io.github.phper666.sforce.api.sdk.serialize.GsonJsonSerializer;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class BulkApiTest {

    private static Response buildResponse(Request request, int code, String body) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code == 401 ? "Unauthorized" : "OK")
                .body(ResponseBody.create(MediaType.parse("application/json"), body))
                .build();
    }

    private static Response buildResponse(Request request, int code, String body, String... headers) {
        Response.Builder builder = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code == 401 ? "Unauthorized" : "OK")
                .body(ResponseBody.create(MediaType.parse("text/csv"), body));
        for (int i = 0; i + 1 < headers.length; i += 2) {
            builder.addHeader(headers[i], headers[i + 1]);
        }
        return builder.build();
    }

    private static SforceApi apiWith(Interceptor interceptor) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken("test-token")
                .setLoginEndpoint("https://test.salesforce.com")
                .setOkHttpClient(client);
        return new SforceApi(config);
    }

    @Test
    void createBulkApiJob() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            String method = request.method();
            counter.incrementAndGet();
            if (method.equals("POST") && path.endsWith("/jobs/ingest")) {
                return buildResponse(request, 200,
                        "{\"id\":\"750xx000000000\",\"contentUrl\":\"services/data/v62.0/jobs/ingest/750xx000000000/batches\"}");
            }
            if (method.equals("PUT")) {
                return buildResponse(request, 200, "");
            }
            if (method.equals("PATCH")) {
                return buildResponse(request, 200,
                        "{\"id\":\"750xx000000000\",\"state\":\"UploadComplete\"}");
            }
            return buildResponse(request, 200, "{}");
        });

        BulkApiCreateJobRequest request = new BulkApiCreateJobRequest()
                .setObject("Account")
                .setOperation(BulkApi.JobOperation.INSERT);

        File csv = File.createTempFile("bulk-upload", ".csv");
        csv.deleteOnExit();
        Files.writeString(csv.toPath(), "Name\nTest");

        BulkApiJobDetailResponse response = api.bulk().createBulkApiJob(request, csv, null);

        assertEquals("750xx000000000", response.getId());
        assertTrue(counter.get() >= 3);
    }

    @Test
    void getBulkApiJob() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            capturedPath.set(request.url().encodedPath());
            return buildResponse(request, 200,
                    "{\"id\":\"750xx000000001\",\"state\":\"JobComplete\",\"object\":\"Account\"}");
        });

        BulkApiJobDetailResponse response = api.bulk().getBulkApiJob("750xx000000001", null);

        assertEquals("750xx000000001", response.getId());
        assertEquals(BulkApi.JobState.JOB_COMPLETE, response.getState());
        assertTrue(capturedPath.get().contains("/jobs/ingest/750xx000000001/"));
    }

    @Test
    void downloadBulkApiJobResult() throws Exception {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            capturedPath.set(request.url().encodedPath());
            return buildResponse(request, 200, "Id,Name\n001xx,Test");
        });

        File dst = File.createTempFile("bulk-result", ".csv");
        dst.deleteOnExit();

        api.bulk().downloadBulkApiJobResult("750xx000000002", BulkApi.JobResultType.SUCCESSFUL_RESULT, dst, null);

        assertTrue(capturedPath.get().contains("/jobs/ingest/750xx000000002/successfulResults"));
        assertEquals("Id,Name\n001xx,Test", Files.readString(dst.toPath()));
    }

    // ── Bulk API 2.0 Query ──

    @Test
    void createBulkQueryJob() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        AtomicReference<String> capturedBody = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            capturedPath.set(request.url().encodedPath());
            capturedBody.set(request.body() != null ? request.body().toString() : "");
            return buildResponse(request, 200,
                    "{\"id\":\"751xx000000001\",\"state\":\"UploadComplete\",\"object\":\"Account\"," +
                            "\"query\":\"SELECT Id FROM Account\",\"operation\":\"query\"}");
        });

        BulkApiQueryJobRequest request = new BulkApiQueryJobRequest()
                .setObject("Account")
                .setQuery("SELECT Id FROM Account");

        BulkApiQueryJobResponse response = api.bulk().createBulkQueryJob(request, null);

        assertEquals("751xx000000001", response.getId());
        assertEquals(BulkApi.JobState.UPLOAD_COMPLETE, response.getState());
        assertEquals("Account", response.getObject());
        assertEquals(BulkApi.JobOperation.QUERY, response.getOperation());
        assertTrue(capturedPath.get().contains("/jobs/query"));
    }

    @Test
    void getBulkQueryJob() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            capturedPath.set(request.url().encodedPath());
            return buildResponse(request, 200,
                    "{\"id\":\"751xx000000002\",\"state\":\"JobComplete\",\"object\":\"Account\"," +
                            "\"numberRecordsProcessed\":5,\"totalProcessingTime\":120}");
        });

        BulkApiQueryJobResponse response = api.bulk().getBulkQueryJob("751xx000000002", null);

        assertEquals("751xx000000002", response.getId());
        assertEquals(BulkApi.JobState.JOB_COMPLETE, response.getState());
        assertEquals(5, response.getNumberRecordsProcessed());
        assertEquals(120, response.getTotalProcessingTime());
        assertTrue(capturedPath.get().contains("/jobs/query/751xx000000002"));
    }

    @Test
    void downloadBulkQueryJobResult() throws Exception {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            capturedPath.set(request.url().encodedPath());
            return buildResponse(request, 200, "Id,Name\n001xx,Test");
        });

        File dst = File.createTempFile("bulk-query-result", ".csv");
        dst.deleteOnExit();

        api.bulk().downloadBulkQueryJobResult("751xx000000003", dst, null);

        assertTrue(capturedPath.get().contains("/jobs/query/751xx000000003/results"));
        assertEquals("Id,Name\n001xx,Test", Files.readString(dst.toPath()));
    }

    @Test
    void deleteBulkQueryJob() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        AtomicReference<String> capturedMethod = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            capturedPath.set(request.url().encodedPath());
            capturedMethod.set(request.method());
            return buildResponse(request, 200, "");
        });

        api.bulk().deleteBulkQueryJob("751xx000000004", null);

        assertTrue(capturedPath.get().contains("/jobs/query/751xx000000004"));
        assertEquals("DELETE", capturedMethod.get());
    }

    @Test
    void downloadBulkQueryJobResultPages() throws Exception {
        AtomicInteger callCount = new AtomicInteger();
        AtomicReference<String> secondUrl = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            int n = callCount.incrementAndGet();
            if (n == 1) {
                return buildResponse(request, 200, "Id,Name\n001,Alice\n002,Bob\n",
                        "Sforce-Locator", "locator1", "Sforce-NumberOfRecords", "2");
            }
            secondUrl.set(request.url().toString());
            return buildResponse(request, 200, "Id,Name\n003,Carol\n", "Sforce-Locator", "null");
        });

        File dst = File.createTempFile("bulk-query-paged", ".csv");
        dst.deleteOnExit();

        api.bulk().downloadBulkQueryJobResult("751xx000000003", dst, null, null);

        assertEquals(2, callCount.get());
        assertTrue(secondUrl.get().contains("?locator=locator1"));
        assertEquals("Id,Name\n001,Alice\n002,Bob\n003,Carol\n", Files.readString(dst.toPath()));
    }

    @Test
    void downloadBulkQueryJobResultMaxRecords() throws Exception {
        AtomicReference<String> capturedUrl = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            capturedUrl.set(request.url().toString());
            return buildResponse(request, 200, "Id,Name\n001,Test\n", "Sforce-Locator", "null");
        });

        File dst = File.createTempFile("bulk-query-max", ".csv");
        dst.deleteOnExit();

        api.bulk().downloadBulkQueryJobResult("751xx000000003", dst, 500, null);

        assertTrue(capturedUrl.get().contains("maxRecords=500"));
    }

    @Test
    void waitForJobComplete() {
        AtomicInteger callCount = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            int n = callCount.incrementAndGet();
            if (n == 1) {
                return buildResponse(chain.request(), 200, "{\"id\":\"751xx000000005\",\"state\":\"InProgress\"}");
            }
            return buildResponse(chain.request(), 200, "{\"id\":\"751xx000000005\",\"state\":\"JobComplete\"}");
        });

        BulkApiQueryJobResponse response = api.bulk().waitForJobComplete("751xx000000005", 10L, 5000L, null);

        assertEquals(BulkApi.JobState.JOB_COMPLETE, response.getState());
        assertTrue(callCount.get() >= 2);
    }

    @Test
    void waitForJobCompleteFailed() {
        SforceApi api = apiWith(chain ->
                buildResponse(chain.request(), 200, "{\"id\":\"751xx000000006\",\"state\":\"Failed\"}"));

        assertThrows(IllegalStateException.class,
                () -> api.bulk().waitForJobComplete("751xx000000006", 10L, 5000L, null));
    }

    @Test
    void queryAllOperationSerialization() {
        BulkApiQueryJobRequest request = new BulkApiQueryJobRequest()
                .setObject("Account")
                .setQuery("SELECT Id FROM Account")
                .setOperation(BulkApi.JobOperation.QUERY_ALL);

        String json = GsonJsonSerializer.INSTANCE().toJson(request);
        assertTrue(json.contains("\"queryAll\""));

        BulkApiQueryJobResponse response = (BulkApiQueryJobResponse) GsonJsonSerializer.INSTANCE()
                .fromJson("{\"operation\":\"queryAll\"}", BulkApiQueryJobResponse.class);
        assertEquals(BulkApi.JobOperation.QUERY_ALL, response.getOperation());
    }

    @Test
    void runQueryJobs() throws Exception {
        AtomicInteger jobCounter = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            String method = request.method();
            if (method.equals("POST") && path.endsWith("/jobs/query")) {
                String id = "751xx00000000" + jobCounter.incrementAndGet();
                return buildResponse(request, 200, "{\"id\":\"" + id + "\",\"state\":\"UploadComplete\"}");
            }
            if (path.endsWith("/results")) {
                String[] parts = path.split("/");
                String jobId = parts[parts.length - 2];
                String name = jobId.endsWith("1") ? "Alice" : "Bob";
                return buildResponse(request, 200, "Id,Name\n" + name + "\n", "Sforce-Locator", "null");
            }
            // GET /jobs/query/{id} → JobComplete immediately, avoids 3s default poll sleep
            return buildResponse(request, 200, "{\"id\":\"751xx000000009\",\"state\":\"JobComplete\"}");
        });

        File dstDir = Files.createTempDirectory("bulk-query-jobs").toFile();
        List<String> queries = List.of("SELECT Id FROM Account", "SELECT Name FROM Contact");

        Map<String, File> results = api.bulk().runQueryJobs(queries, "Account", dstDir, 2, null, null);

        assertEquals(2, results.size());
        assertEquals("Id,Name\nAlice\n", Files.readString(results.get(queries.get(0)).toPath()));
        assertEquals("Id,Name\nBob\n", Files.readString(results.get(queries.get(1)).toPath()));
        for (File f : results.values()) {
            assertTrue(f.exists());
            assertTrue(f.length() > 0);
        }
    }

    @Test
    void waitForJobCompleteTimeout() {
        SforceApi api = apiWith(chain ->
                buildResponse(chain.request(), 200, "{\"id\":\"751xx000000007\",\"state\":\"InProgress\"}"));

        assertThrows(IllegalStateException.class,
                () -> api.bulk().waitForJobComplete("751xx000000007", 10L, 100L, null));
    }

    @Test
    void jobStateInProgressDeserializes() {
        BulkApiQueryJobResponse response = (BulkApiQueryJobResponse) GsonJsonSerializer.INSTANCE()
                .fromJson("{\"id\":\"751xx000000008\",\"state\":\"InProgress\"}", BulkApiQueryJobResponse.class);
        assertEquals(BulkApi.JobState.IN_PROGRESS, response.getState());
    }

    // ── forEachResultPage / QueryResultIterator ──

    private SforceApi apiWithBulkQueryMock(AtomicInteger deleteCount) {
        return apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            String method = request.method();
            if (method.equals("DELETE") && path.endsWith("/jobs/query/751xx000000010")) {
                deleteCount.incrementAndGet();
                return buildResponse(request, 200, "");
            }
            if (path.endsWith("/results")) {
                String locator = request.url().queryParameter("locator");
                if (locator == null) {
                    // 第一页：两行数据 + 下一页 token
                    return buildResponse(request, 200,
                            "Id,Name\n001,Alice\n002,Bob\n",
                            "Sforce-Locator", "page2-token");
                }
                assertEquals("page2-token", locator, "second page must pass the locator back");
                // 第二页：一行数据 + 结束标记
                return buildResponse(request, 200,
                        "Id,Name\n003,Carol\n",
                        "Sforce-Locator", "null");
            }
            // GET job status → JobComplete, numberRecordsProcessed=3
            return buildResponse(request, 200,
                    "{\"id\":\"751xx000000010\",\"state\":\"JobComplete\",\"numberRecordsProcessed\":3}");
        });
    }

    @Test
    void forEachResultPagePagesThroughLocator() {
        AtomicInteger deleteCount = new AtomicInteger();
        SforceApi api = apiWithBulkQueryMock(deleteCount);
        List<List<JsonObject>> pages = new ArrayList<>();

        api.bulk().forEachResultPage("751xx000000010", null, null, null,
                pages::add, null, true);

        assertEquals(2, pages.size(), "should receive 2 pages");
        assertEquals(2, pages.get(0).size());
        assertEquals(1, pages.get(1).size());
        assertEquals("001", pages.get(0).get(0).get("Id").getAsString());
        assertEquals("Alice", pages.get(0).get(0).get("Name").getAsString());
        assertEquals("003", pages.get(1).get(0).get("Id").getAsString());
        assertEquals(1, deleteCount.get(), "deleteOnComplete=true must delete the job after full consumption");
    }

    @Test
    void forEachResultPageDeleteOnCompleteFalseKeepsJob() {
        AtomicInteger deleteCount = new AtomicInteger();
        SforceApi api = apiWithBulkQueryMock(deleteCount);

        api.bulk().forEachResultPage("751xx000000010", null, null, null,
                p -> { }, null, false);

        assertEquals(0, deleteCount.get(), "deleteOnComplete=false must not delete the job");
    }

    @Test
    void forEachResultPageConsumerExceptionKeepsJob() {
        AtomicInteger deleteCount = new AtomicInteger();
        SforceApi api = apiWithBulkQueryMock(deleteCount);

        assertThrows(IllegalStateException.class, () -> api.bulk().forEachResultPage(
                "751xx000000010", null, null, null,
                p -> { throw new IllegalStateException("consumer failed"); },
                null, true));

        assertEquals(0, deleteCount.get(), "failure must NOT delete the job, even with deleteOnComplete=true");
    }

    @Test
    void forEachResultPageRowCountMismatchThrowsAndKeepsJob() {
        AtomicInteger deleteCount = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            if (path.endsWith("/results")) {
                // 只返回一页两行，但 job 声称处理了 3 行
                return buildResponse(request, 200,
                        "Id,Name\n001,Alice\n002,Bob\n",
                        "Sforce-Locator", "null");
            }
            return buildResponse(request, 200,
                    "{\"id\":\"751xx000000010\",\"state\":\"JobComplete\",\"numberRecordsProcessed\":3}");
        });

        assertThrows(IllegalStateException.class, () -> api.bulk().forEachResultPage(
                "751xx000000010", null, null, null, p -> { }, null, true));

        assertEquals(0, deleteCount.get(), "row-count mismatch must NOT delete the job");
    }

    @Test
    void queryResultIteratorFullConsumptionDeletesOnClose() {
        AtomicInteger deleteCount = new AtomicInteger();
        SforceApi api = apiWithBulkQueryMock(deleteCount);
        List<JsonObject> all = new ArrayList<>();

        try (BulkApi.QueryResultIterator it = api.bulk()
                .queryResultIterator("751xx000000010", null, null, null, null, true)) {
            while (it.hasNext()) {
                all.addAll(it.next());
            }
        }

        assertEquals(3, all.size());
        assertEquals(1, deleteCount.get(), "full consumption + deleteOnComplete must delete on close");
    }

    @Test
    void queryResultIteratorEarlyBreakKeepsJob() {
        AtomicInteger deleteCount = new AtomicInteger();
        SforceApi api = apiWithBulkQueryMock(deleteCount);

        try (BulkApi.QueryResultIterator it = api.bulk()
                .queryResultIterator("751xx000000010", null, null, null, null, true)) {
            if (it.hasNext()) {
                it.next(); // 只消费第一页就提前退出
            }
        }

        assertEquals(0, deleteCount.get(), "early break must NOT delete the job, even with deleteOnComplete=true");
    }

    @Test
    void queryResultIteratorNoSuchElement() {
        SforceApi api = apiWithBulkQueryMock(new AtomicInteger());
        try (BulkApi.QueryResultIterator it = api.bulk()
                .queryResultIterator("751xx000000010", null, null, null, null, false)) {
            while (it.hasNext()) {
                it.next();
            }
            assertThrows(java.util.NoSuchElementException.class, it::next);
        }
    }

    // ── CSV 解析边界（parseCsvToJson 经 forEachResultPage 覆盖）──

    @Test
    void csvQuotedNewlineParsedAsSingleRecord() {
        AtomicInteger deleteCount = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            if (path.endsWith("/results")) {
                // 长文本字段：引号内包含换行，必须解析为一条记录
                return buildResponse(request, 200,
                        "Id,Name,Description\n001,Alice,\"line1\nline2\"\n",
                        "Sforce-Locator", "null");
            }
            return buildResponse(request, 200,
                    "{\"id\":\"751xx000000011\",\"state\":\"JobComplete\",\"numberRecordsProcessed\":1}");
        });
        List<List<JsonObject>> pages = new ArrayList<>();

        api.bulk().forEachResultPage("751xx000000011", null, null, null, pages::add, null, false);

        assertEquals(1, pages.get(0).size(), "quoted newline must stay one record");
        assertEquals("line1\nline2", pages.get(0).get(0).get("Description").getAsString());
    }

    @Test
    void csvNulBytesStrippedBeforeParsing() {
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            if (path.endsWith("/results")) {
                // NUL 字节（\u0000）应被剥除后再解析，见 airbyte#8300
                return buildResponse(request, 200,
                        "Id,Name\n001,Alice\u0000X\n",
                        "Sforce-Locator", "null");
            }
            return buildResponse(request, 200,
                    "{\"id\":\"751xx000000012\",\"state\":\"JobComplete\",\"numberRecordsProcessed\":1}");
        });
        List<List<JsonObject>> pages = new ArrayList<>();

        api.bulk().forEachResultPage("751xx000000012", null, null, null, pages::add, null, false);

        assertEquals("AliceX", pages.get(0).get(0).get("Name").getAsString(),
                "NUL byte must be stripped from field values");
    }

    @Test
    void csvEmptyResultYieldsZeroRecords() {
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            if (path.endsWith("/results")) {
                // 仅表头、无数据行
                return buildResponse(request, 200, "Id,Name\n", "Sforce-Locator", "null");
            }
            return buildResponse(request, 200,
                    "{\"id\":\"751xx000000013\",\"state\":\"JobComplete\",\"numberRecordsProcessed\":0}");
        });
        List<List<JsonObject>> pages = new ArrayList<>();

        api.bulk().forEachResultPage("751xx000000013", null, null, null, pages::add, null, false);

        assertEquals(0, pages.get(0).size(), "header-only CSV must parse to zero records");
    }

    @Test
    void queryResultIteratorRowCountMismatchThrows() {
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            if (path.endsWith("/results")) {
                // 一页两行，但 job 声称 3 行
                return buildResponse(request, 200,
                        "Id,Name\n001,Alice\n002,Bob\n",
                        "Sforce-Locator", "null");
            }
            return buildResponse(request, 200,
                    "{\"id\":\"751xx000000014\",\"state\":\"JobComplete\",\"numberRecordsProcessed\":3}");
        });

        try (BulkApi.QueryResultIterator it = api.bulk()
                .queryResultIterator("751xx000000014", null, null, null, null, false)) {
            assertThrows(IllegalStateException.class, () -> {
                while (it.hasNext()) {
                    it.next();
                }
            });
        }
    }

    // ── resultPages 并行下载（API v58.0+）──

    private static final String RP_JOB = "751xx000000020";

    private static String resultUrl(String locator) {
        return "/services/data/v62.0/jobs/query/" + RP_JOB + "/results?locator=" + locator;
    }

    private static String resultPagesUrl(String locator) {
        return "/services/data/v62.0/jobs/query/" + RP_JOB + "/resultPages?locator=" + locator;
    }

    /** 按 locator 返回不同的 CSV 段 */
    private static Response segmentResponse(Request request, String locator) {
        return switch (locator) {
            case "AAA" -> buildResponse(request, 200, "Id,Name\n001,Alice\n");
            case "BBB" -> buildResponse(request, 200, "Id,Name\n002,Bob\n");
            case "CCC" -> buildResponse(request, 200, "Id,Name\n003,Carol\n");
            default -> buildResponse(request, 200, "Id,Name\nunknown\n");
        };
    }

    @Test
    void downloadBulkQueryJobResultParallelMultiRound() throws Exception {
        AtomicInteger rpCalls = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            if (path.endsWith("/resultPages")) {
                int n = rpCalls.incrementAndGet();
                if (n == 1) {
                    // 第 1 轮：2 个 resultUrl + done:false + nextRecordsUrl
                    return buildResponse(request, 200,
                            "{\"resultPages\":[{\"resultUrl\":\"" + resultUrl("AAA") + "\"},"
                                    + "{\"resultUrl\":\"" + resultUrl("BBB") + "\"}],"
                                    + "\"nextRecordsUrl\":\"" + resultPagesUrl("CCC") + "\",\"done\":false}");
                }
                // 第 2 轮：1 个 resultUrl + done:true
                return buildResponse(request, 200,
                        "{\"resultPages\":[{\"resultUrl\":\"" + resultUrl("CCC") + "\"}],\"done\":true}");
            }
            return segmentResponse(request, request.url().queryParameter("locator"));
        });

        File dstDir = Files.createTempDirectory("bulk-rp").toFile();
        File dst = new File(dstDir, "nested/out.csv"); // 父目录不存在 → 应自动创建

        api.bulk().downloadBulkQueryJobResultParallel(RP_JOB, dst, 2, null);

        assertEquals(2, rpCalls.get(), "resultPages must be paged over two rounds");
        assertEquals("Id,Name\n001,Alice\n002,Bob\n003,Carol\n", Files.readString(dst.toPath()));
    }

    @Test
    void downloadBulkQueryJobResultParallelNextRecordUrlCompat() throws Exception {
        AtomicInteger rpCalls = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            String path = request.url().encodedPath();
            if (path.endsWith("/resultPages")) {
                int n = rpCalls.incrementAndGet();
                if (n == 1) {
                    // 官方表格字段名 nextRecordUrl（单数）也要能翻页
                    return buildResponse(request, 200,
                            "{\"resultPages\":[{\"resultUrl\":\"" + resultUrl("AAA") + "\"}],"
                                    + "\"nextRecordUrl\":\"" + resultPagesUrl("BBB") + "\",\"done\":false}");
                }
                return buildResponse(request, 200,
                        "{\"resultPages\":[{\"resultUrl\":\"" + resultUrl("BBB") + "\"}],\"done\":true}");
            }
            return segmentResponse(request, request.url().queryParameter("locator"));
        });

        File dst = File.createTempFile("bulk-rp-compat", ".csv");
        dst.deleteOnExit();

        api.bulk().downloadBulkQueryJobResultParallel(RP_JOB, dst, 2, null);

        assertEquals(2, rpCalls.get(), "must continue paging via nextRecordUrl");
        assertEquals("Id,Name\n001,Alice\n002,Bob\n", Files.readString(dst.toPath()));
    }

    @Test
    void downloadBulkQueryJobResultParallelDedupsHeader() throws Exception {
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            if (request.url().encodedPath().endsWith("/resultPages")) {
                return buildResponse(request, 200,
                        "{\"resultPages\":[{\"resultUrl\":\"" + resultUrl("AAA") + "\"},"
                                + "{\"resultUrl\":\"" + resultUrl("BBB") + "\"}],\"done\":true}");
            }
            return segmentResponse(request, request.url().queryParameter("locator"));
        });

        File dst = File.createTempFile("bulk-rp-header", ".csv");
        dst.deleteOnExit();

        api.bulk().downloadBulkQueryJobResultParallel(RP_JOB, dst, 2, null);

        String content = Files.readString(dst.toPath());
        assertEquals(1, content.split("Id,Name", -1).length - 1, "file must contain exactly one header row");
        assertEquals("Id,Name\n001,Alice\n002,Bob\n", content);
    }

    @Test
    void downloadBulkQueryJobResultParallelKeepsOrderDespiteOutOfOrderCompletion() throws Exception {
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            if (request.url().encodedPath().endsWith("/resultPages")) {
                return buildResponse(request, 200,
                        "{\"resultPages\":[{\"resultUrl\":\"" + resultUrl("AAA") + "\"},"
                                + "{\"resultUrl\":\"" + resultUrl("BBB") + "\"}],\"done\":true}");
            }
            String locator = request.url().queryParameter("locator");
            if ("AAA".equals(locator)) {
                try {
                    Thread.sleep(200); // 第一段最慢完成 → 检验写文件仍按 resultPages 顺序
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return segmentResponse(request, locator);
        });

        File dst = File.createTempFile("bulk-rp-order", ".csv");
        dst.deleteOnExit();

        api.bulk().downloadBulkQueryJobResultParallel(RP_JOB, dst, 2, null);

        assertEquals("Id,Name\n001,Alice\n002,Bob\n", Files.readString(dst.toPath()));
    }

    @Test
    void downloadBulkQueryJobResultParallelEmptyResult() throws Exception {
        SforceApi api = apiWith(chain ->
                buildResponse(chain.request(), 200, "{\"resultPages\":[],\"done\":true}"));

        File dst = File.createTempFile("bulk-rp-empty", ".csv");
        dst.deleteOnExit();

        api.bulk().downloadBulkQueryJobResultParallel(RP_JOB, dst, null, null);

        assertTrue(dst.exists());
        assertEquals(0, dst.length(), "empty resultPages must produce an empty file, no error");
    }

    @Test
    void downloadBulkQueryJobResultParallelRetriesFailedSegment() throws Exception {
        AtomicInteger aaaCount = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            if (request.url().encodedPath().endsWith("/resultPages")) {
                return buildResponse(request, 200,
                        "{\"resultPages\":[{\"resultUrl\":\"" + resultUrl("AAA") + "\"}],\"done\":true}");
            }
            if ("AAA".equals(request.url().queryParameter("locator"))) {
                if (aaaCount.incrementAndGet() == 1) {
                    return buildResponse(request, 500, "server error");
                }
                return buildResponse(request, 200, "Id,Name\n001,Alice\n");
            }
            return buildResponse(request, 200, "");
        });

        File dst = File.createTempFile("bulk-rp-retry", ".csv");
        dst.deleteOnExit();

        api.bulk().downloadBulkQueryJobResultParallel(RP_JOB, dst, 1, null);

        assertEquals(2, aaaCount.get(), "failed segment must be retried once and then succeed");
        assertEquals("Id,Name\n001,Alice\n", Files.readString(dst.toPath()));
    }

    @Test
    void forEachResultRowStreamsRowsInOrder() throws Exception {
        AtomicInteger deleteCount = new AtomicInteger();
        SforceApi api = apiWithBulkQueryMock(deleteCount);
        List<JsonObject> rows = new ArrayList<>();

        api.bulk().forEachResultRow("751xx000000010", null, rows::add, null);

        assertEquals(3, rows.size());
        assertEquals("001", rows.get(0).get("Id").getAsString());
        assertEquals("Alice", rows.get(0).get("Name").getAsString());
        assertEquals("002", rows.get(1).get("Id").getAsString());
        assertEquals("003", rows.get(2).get("Id").getAsString());
    }

    @Test
    void forEachResultPageParallelDeliversPagesInOrder() throws Exception {
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            if (request.url().encodedPath().endsWith("/resultPages")) {
                return buildResponse(request, 200,
                        "{\"resultPages\":[{\"resultUrl\":\"" + resultUrl("AAA") + "\"},"
                                + "{\"resultUrl\":\"" + resultUrl("BBB") + "\"}],\"done\":true}");
            }
            return segmentResponse(request, request.url().queryParameter("locator"));
        });
        List<List<JsonObject>> pages = new ArrayList<>();

        api.bulk().forEachResultPageParallel(RP_JOB, 2, pages::add, null);

        assertEquals(2, pages.size());
        assertEquals(1, pages.get(0).size());
        assertEquals("001", pages.get(0).get(0).get("Id").getAsString());
        assertEquals("Alice", pages.get(0).get(0).get("Name").getAsString());
        assertEquals(1, pages.get(1).size());
        assertEquals("002", pages.get(1).get(0).get("Id").getAsString());
    }
}
