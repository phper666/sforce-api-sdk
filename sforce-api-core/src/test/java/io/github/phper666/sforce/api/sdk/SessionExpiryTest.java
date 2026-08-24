package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionExpiryTest {

    @Test
    void sessionWithoutExpiryNeverExpired() {
        Session session = new Session("token", "https://test.salesforce.com");
        assertFalse(session.isExpired());
    }

    @Test
    void sessionWithFutureExpiryNotExpired() {
        Session session = new Session("token", "https://test.salesforce.com",
                System.currentTimeMillis() + 300_000);
        assertFalse(session.isExpired());
    }

    @Test
    void sessionWithPastExpiryExpired() {
        Session session = new Session("token", "https://test.salesforce.com",
                System.currentTimeMillis() - 5_000);
        assertTrue(session.isExpired());
    }

    @Test
    void sessionWithinSafetyMarginExpired() {
        // 30s left, but safety margin is 60s → should be treated as expired
        Session session = new Session("token", "https://test.salesforce.com",
                System.currentTimeMillis() + 30_000);
        assertTrue(session.isExpired());
    }

    @Test
    void sessionExactlyAtSafetyMarginBoundaryExpired() {
        // Exactly 60s left == safety margin → boundary should be treated as expired
        Session session = new Session("token", "https://test.salesforce.com",
                System.currentTimeMillis() + 60_000);
        assertTrue(session.isExpired());
    }
}