package com.phper666.sforce.api.sdk.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void authException() {
        AuthException e = new AuthException(401, "Invalid credentials");
        assertEquals(401, e.getCode());
        assertEquals("Invalid credentials", e.getMessage());
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
