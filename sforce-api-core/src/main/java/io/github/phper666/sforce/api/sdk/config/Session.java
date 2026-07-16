package io.github.phper666.sforce.api.sdk.config;

/**
 * Record holding access token and API endpoint for a session.
 *
 * @param accessToken  the OAuth access token
 * @param apiEndpoint  the Salesforce API endpoint URL
 * @author Yuzhao.Li
 */
public record Session(String accessToken, String apiEndpoint) {}
