package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.Session;

import com.google.gson.annotations.SerializedName;
import com.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;
import com.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import com.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import com.phper666.sforce.api.sdk.internal.BaseApi;
import com.phper666.sforce.api.sdk.model.BulkApiCreateJobRequest;
import com.phper666.sforce.api.sdk.model.BulkApiJobDetailResponse;
import com.phper666.sforce.api.sdk.serialize.JsonSerializer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        body.put("state", JobState.UPDATE_COMPLETE);
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
        @SerializedName("upsert") UPSERT
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
        @SerializedName("UploadComplete") UPDATE_COMPLETE,
        @SerializedName("Aborted") ABORTED,
        @SerializedName("JobComplete") JOB_COMPLETE,
        @SerializedName("Failed") FAILED
    }
}
