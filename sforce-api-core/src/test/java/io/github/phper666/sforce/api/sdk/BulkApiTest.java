package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.model.BulkApiCreateJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiJobDetailResponse;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobResponse;
import io.github.phper666.sforce.api.sdk.serialize.GsonJsonSerializer;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
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
}
