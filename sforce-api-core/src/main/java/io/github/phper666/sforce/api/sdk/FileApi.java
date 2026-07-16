package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.Session;

import io.github.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;
import io.github.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import io.github.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import io.github.phper666.sforce.api.sdk.exception.DownloadFileException;
import io.github.phper666.sforce.api.sdk.internal.BaseApi;
import io.github.phper666.sforce.api.sdk.internal.FileUtils;
import io.github.phper666.sforce.api.sdk.model.DownloadContentDocumentRequest;
import io.github.phper666.sforce.api.sdk.serialize.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

@Slf4j
class FileApi extends BaseApi {
    FileApi(SdkConfig config, Session session, OkHttpClient okHttpClient, JsonSerializer jsonSerializer, BaseAuthenticator authFlow) {
        super(config, session, okHttpClient, jsonSerializer, authFlow);
    }

    // ──────────────────────────────────────────────
    // Chatter File
    // ──────────────────────────────────────────────

    public String uploadChatterFile(File file) {
        return uploadChatterFile(file, DEFAULT_TIME_OUT);
    }

    public String uploadChatterFile(File file, TimeoutSettings timeOutConfig) {
        RequestBody rb = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileData", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file))
                .build();
        try {
            String response = executeGetBody(chatterFileUploadUrl(), HttpMethod.POST.name(), rb, EMPTY_HEADERS, timeOutConfig);
            Map<?, ?> result = (Map<?, ?>) jsonSerializer.fromJson(response, Map.class);
            return (String) result.get("id");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateChatterFileDownloadUrl(String id) {
        return session.apiEndpoint() + "/sfc/servlet.shepherd/document/download/" + id;
    }

    // ──────────────────────────────────────────────
    // ContentDocument download
    // ──────────────────────────────────────────────

    public File downloadContentDocument(DownloadContentDocumentRequest request) {
        return downloadContentDocument(request, DEFAULT_TIME_OUT);
    }

    public File downloadContentDocument(DownloadContentDocumentRequest request, TimeoutSettings timeOutConfig) {
        String url = getFileContentUrl(request.fileId());
        try (Response response = execute(url, HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, timeOutConfig)) {
            return createTempFile(request, response);
        } catch (IOException e) {
            log.error("download file exception, fileId:{}", request.fileId(), e);
            throw new DownloadFileException(e.getMessage());
        }
    }

    public File downloadContentDocumentFile(String fileId) {
        return downloadContentDocumentFile(fileId, DEFAULT_TIME_OUT);
    }

    public File downloadContentDocumentFile(String fileId, TimeoutSettings timeOutConfig) {
        String url = getFileContentUrl(fileId);
        try (Response response = execute(url, HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, timeOutConfig)) {
            var responseBody = Optional.ofNullable(response.body())
                    .orElseThrow(() -> new DownloadFileException("download file error, fileId: " + fileId));
            var fileName = extractFileNameFromHeader(response.header("Content-Disposition"));
            if (fileName != null) {
                var file = File.createTempFile(FileUtils.fileMainName(fileName), "." + FileUtils.fileExtName(fileName));
                writeResponseToFile(file, responseBody);
                return file;
            }
            throw new DownloadFileException("download file error, fileId: " + fileId);
        } catch (IOException e) {
            log.error("downloadContentDocumentFile exception, fileId:{}", fileId, e);
            throw new DownloadFileException(e.getMessage());
        }
    }

    private File createTempFile(DownloadContentDocumentRequest request, Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new DownloadFileException("download file error, fileId: " + request.fileId());
        }
        Path filePath = Files.createTempFile(Paths.get(request.directory()), request.prefix(), request.suffix());
        File file = filePath.toFile();
        writeResponseToFile(file, responseBody);
        return file;
    }

    private static void writeResponseToFile(File file, ResponseBody responseBody) throws IOException {
        try (BufferedSink sink = Okio.buffer(Okio.sink(file))) {
            sink.writeAll(Okio.source(responseBody.byteStream()));
        }
    }

    private String extractFileNameFromHeader(String contentDisposition) {
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            contentDisposition = URLDecoder.decode(contentDisposition, StandardCharsets.UTF_8);
            for (String part : contentDisposition.split("; ")) {
                if (part.trim().startsWith("filename=")) {
                    return part.substring(part.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }

}
