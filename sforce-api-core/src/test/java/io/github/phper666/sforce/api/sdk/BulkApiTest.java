package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.model.BulkApiCreateJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiJobDetailResponse;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
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
}
