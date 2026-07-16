package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SforceApiManager;
import io.github.phper666.sforce.api.sdk.config.Session;
import io.github.phper666.sforce.api.sdk.config.SdkTypes;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SforceApiManagerTest {

    @BeforeEach
    void setUp() {
        SforceApiManager.clear();
    }

    private SdkConfig accessTokenConfig(String token) {
        return new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(token)
                .setLoginEndpoint("https://testinstance.salesforce.com")
                .setOkHttpClient(new OkHttpClient());
    }

    @Test
    void computeIfAbsentCreatesNewInstanceForUnknownKey() {
        SdkConfig config = accessTokenConfig("token1");
        SforceApi api = SforceApiManager.computeIfAbsent("key1", config);
        assertNotNull(api);
        assertEquals("token1", api.getAccessToken());
        assertEquals("https://testinstance.salesforce.com", api.getApiEndpoint());
    }

    @Test
    void computeIfAbsentReturnsCachedInstanceForSameKey() {
        SdkConfig config1 = accessTokenConfig("token1");
        SdkConfig config2 = accessTokenConfig("token2");

        SforceApi api1 = SforceApiManager.computeIfAbsent("shared-key", config1);
        SforceApi api2 = SforceApiManager.computeIfAbsent("shared-key", config2);

        assertSame(api1, api2);
        assertEquals("token1", api2.getAccessToken());
    }

    @Test
    void computeIfAbsentReturnsDifferentInstanceForDifferentKey() {
        SdkConfig config1 = accessTokenConfig("token1");
        SdkConfig config2 = accessTokenConfig("token2");

        SforceApi api1 = SforceApiManager.computeIfAbsent("key1", config1);
        SforceApi api2 = SforceApiManager.computeIfAbsent("key2", config2);

        assertNotNull(api1);
        assertNotNull(api2);
        assertNotSame(api1, api2);
    }

    @Test
    void clearRemovesAllCachedInstances() {
        SdkConfig config1 = accessTokenConfig("token1");
        SdkConfig config2 = accessTokenConfig("token2");

        SforceApi api1 = SforceApiManager.computeIfAbsent("key1", config1);
        SforceApiManager.clear();
        SforceApi api2 = SforceApiManager.computeIfAbsent("key1", config2);

        assertNotSame(api1, api2);
        assertEquals("token2", api2.getAccessToken());
    }

    @Test
    void cacheIsConcurrentHashMap() throws Exception {
        Field cacheField = SforceApiManager.class.getDeclaredField("CACHE");
        cacheField.setAccessible(true);
        Object cache = cacheField.get(null);
        assertInstanceOf(ConcurrentHashMap.class, cache);
    }

    @Test
    void clearDoesNotThrow() {
        SforceApiManager.clear();
        assertDoesNotThrow(() -> SforceApiManager.clear());
    }
}
