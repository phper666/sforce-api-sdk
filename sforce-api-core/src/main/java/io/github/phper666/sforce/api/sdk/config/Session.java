package io.github.phper666.sforce.api.sdk.config;

/**
 * Record holding access token, API endpoint and expiry time for a session.
 *
 * @param accessToken  the OAuth access token
 * @param apiEndpoint  the Salesforce API endpoint URL
 * @param expiresAt    token expiry timestamp in epoch millis (0 = unknown)
 * @author Yuzhao.Li
 */
public record Session(String accessToken, String apiEndpoint, long expiresAt) {

    public Session(String accessToken, String apiEndpoint) {
        this(accessToken, apiEndpoint, 0);
    }

    /**
     * Whether the access token has expired (or expires within the safety margin).
     *
     * @return true when expiry is known and the token is expired or about to expire
     */
    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt - EXPIRY_SAFETY_MARGIN_MS;
    }

    private static final long EXPIRY_SAFETY_MARGIN_MS = 60_000;
}