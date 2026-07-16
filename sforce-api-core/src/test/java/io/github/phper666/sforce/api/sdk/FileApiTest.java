package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.exception.DownloadFileException;
import io.github.phper666.sforce.api.sdk.model.DownloadContentDocumentRequest;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class FileApiTest {

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
    void uploadChatterFile() throws Exception {
        SforceApi api = apiWith(chain ->
                buildResponse(chain.request(), 200, "{\"id\":\"069xx0000000001\"}"));

        File file = File.createTempFile("chatter", ".txt");
        file.deleteOnExit();
        Files.writeString(file.toPath(), "hello");

        String id = api.file().uploadChatterFile(file);
        assertEquals("069xx0000000001", id);
    }

    @Test
    void downloadContentDocument() throws Exception {
        SforceApi api = apiWith(chain ->
                buildResponse(chain.request(), 200, "pdf-binary-content"));

        Path dir = Files.createTempDirectory("sforce-doc");
        DownloadContentDocumentRequest request = new DownloadContentDocumentRequest(
                "069xx0000000002", dir.toString(), "doc", ".pdf");

        File file = api.file().downloadContentDocument(request);
        file.deleteOnExit();

        assertTrue(file.getName().startsWith("doc"));
        assertTrue(file.getName().endsWith(".pdf"));
        assertEquals("pdf-binary-content", Files.readString(file.toPath()));
    }

    @Test
    void downloadContentDocumentFile() throws Exception {
        AtomicReference<Request> captured = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            Request request = chain.request();
            captured.set(request);
            Response response = new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .header("Content-Disposition", "attachment; filename=\"report.txt\"")
                    .body(ResponseBody.create(MediaType.parse("text/plain"), "report-content"))
                    .build();
            return response;
        });

        File file = api.file().downloadContentDocumentFile("069xx0000000003");
        file.deleteOnExit();

        assertTrue(captured.get().url().encodedPath().contains("/connect/files/069xx0000000003/content"));
        assertTrue(file.getName().startsWith("report"));
        assertTrue(file.getName().endsWith(".txt"));
        assertEquals("report-content", Files.readString(file.toPath()));
    }

    @Test
    void downloadContentDocumentFileWithoutContentDispositionThrows() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200, "body"));

        DownloadFileException exception = assertThrows(DownloadFileException.class,
                () -> api.file().downloadContentDocumentFile("069xx0000000004"));
        assertTrue(exception.getMessage().contains("069xx0000000004"));
    }
}
