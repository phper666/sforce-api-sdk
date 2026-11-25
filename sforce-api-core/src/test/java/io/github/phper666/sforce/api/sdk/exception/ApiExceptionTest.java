package io.github.phper666.sforce.api.sdk.exception;

import io.github.phper666.sforce.api.sdk.model.ApiErrorDetail;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiExceptionTest {

    @Test
    void apiException() {
        ApiException e = new ApiException(400, "Bad Request", "POST",
                "https://test.salesforce.com/services/data/v62.0/sobjects/Account");
        assertEquals(400, e.getCode());
        assertEquals("Bad Request", e.getMessage());
        assertEquals("POST", e.getMethod());
        assertEquals("https://test.salesforce.com/services/data/v62.0/sobjects/Account", e.getUrl());
        assertTrue(e.toString().contains("400"));
        assertTrue(e.toString().contains("POST"));
    }

    @Test
    void apiExceptionWithoutDetailsStaysBackwardCompatible() {
        ApiException e = new ApiException(400, "Bad Request", "POST", "https://x");

        assertNull(e.getErrorCode());
        assertTrue(e.getErrorDetails().isEmpty());
        assertEquals("Bad Request", e.getRawBody());
        assertEquals("Bad Request", e.getMessage());
    }

    @Test
    void apiExceptionExposesErrorDetails() {
        ApiException e = new ApiException(400, "[{\"errorCode\":\"MALFORMED_ID\"}]", "POST", "https://x",
                List.of(new ApiErrorDetail("MALFORMED_ID", "bad id", List.of("Id"))));

        assertEquals("MALFORMED_ID", e.getErrorCode());
        assertEquals(1, e.getErrorDetails().size());
        assertEquals(List.of("Id"), e.getErrorDetails().get(0).fields());
        assertEquals("[{\"errorCode\":\"MALFORMED_ID\"}]", e.getRawBody());
    }

    @Test
    void apiExceptionTreatsNullDetailsAsEmpty() {
        ApiException e = new ApiException(400, "raw", "POST", "https://x", null);

        assertNull(e.getErrorCode());
        assertTrue(e.getErrorDetails().isEmpty());
    }

    @Test
    void authException() {
        AuthException e = new AuthException(401, "Invalid credentials");
        assertEquals(401, e.getCode());
        assertEquals("Invalid credentials", e.getMessage());
    }

    @Test
    void authExceptionKeepsCause() {
        IOException io = new IOException("connection reset");
        AuthException e = new AuthException(0, "auth login flow failed: connection reset", io);

        assertEquals(0, e.getCode());
        assertSame(io, e.getCause());
        assertEquals("auth login flow failed: connection reset", e.getMessage());
    }

    @Test
    void configException() {
        ConfigException e = new ConfigException("Invalid config");
        assertEquals("Invalid config", e.getMessage());
        assertTrue(e.toString().contains("ConfigException"));
    }

    @Test
    void downloadFileExceptionWithMessage() {
        DownloadFileException e = new DownloadFileException("File not found");
        assertEquals("File not found", e.getMessage());
        assertEquals(0, e.getCode());
    }

    @Test
    void downloadFileExceptionWithCodeAndMessage() {
        DownloadFileException e = new DownloadFileException(404, "Not found");
        assertEquals(404, e.getCode());
        assertEquals("Not found", e.getMessage());
    }
}
