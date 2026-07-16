package io.github.phper666.sforce.api.sdk.config;

/**
 * Record holding access token and API endpoint for a session.
 *
 * @author Yuzhao.Li
 */
public record Session(String accessToken, String apiEndpoint) {}
