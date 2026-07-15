package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.Session;
import com.phper666.sforce.api.sdk.config.AuthFlow;
import com.phper666.sforce.api.sdk.config.SdkTypes;
import com.phper666.sforce.api.sdk.config.SforceApiManager;

import com.phper666.sforce.api.sdk.serialize.GsonJsonSerializer;
import com.phper666.sforce.api.sdk.serialize.JsonSerializer;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiConfigTest {

    @Test
    void defaultValues() {
        SdkConfig config = new SdkConfig();
        assertEquals("v62.0", config.getApiVersion());
        assertEquals("https://login.salesforce.com", config.getLoginEndpoint());
        assertEquals(AuthFlow.CLIENT_CREDENTIAL, config.getAuthFlow());
    }

    @Test
    void debugDefaults() {
        SdkConfig config = new SdkConfig();
        assertFalse(config.isDebug());
        assertFalse(config.isDebugLogBody());
        assertEquals(4096, config.getDebugBodyMaxSize());
    }

    @Test
    void debugSetters() {
        SdkConfig config = new SdkConfig()
                .setDebug(true)
                .setDebugLogBody(true)
                .setDebugBodyMaxSize(8192);
        assertTrue(config.isDebug());
        assertTrue(config.isDebugLogBody());
        assertEquals(8192, config.getDebugBodyMaxSize());
    }

    @Test
    void fluentSettersReturnThis() {
        SdkConfig config = new SdkConfig();
        assertSame(config, config.setApiVersion("v61.0"));
        assertSame(config, config.setUsername("u"));
        assertSame(config, config.setPassword("p"));
        assertSame(config, config.setLoginEndpoint("https://test.salesforce.com"));
        assertSame(config, config.setAuthFlow(AuthFlow.PASSWORD));
        assertSame(config, config.setClientId("id"));
        assertSame(config, config.setClientSecret("secret"));
        assertSame(config, config.setAuthorizationCode("code"));
        assertSame(config, config.setRedirectUri("https://app.com/callback"));
        assertSame(config, config.setJsonSerializer(GsonJsonSerializer.INSTANCE()));
        assertSame(config, config.setCustomObjectNamespace("ns"));
        assertSame(config, config.setDomain("https://domain.my.salesforce.com"));
        assertSame(config, config.setAccessToken("token"));
        assertSame(config, config.setDebug(true));
        assertSame(config, config.setDebugLogBody(true));
        assertSame(config, config.setDebugBodyMaxSize(1024));
        assertSame(config, config.setOkHttpClient(new OkHttpClient()));
    }

    @Test
    void customObjectNamespacePrefix() {
        SdkConfig config = new SdkConfig();
        assertEquals("", config.getCustomObjectNamespacePrefix());

        config.setCustomObjectNamespace("myns");
        assertEquals("myns__", config.getCustomObjectNamespacePrefix());
    }

    @Test
    void defaultJsonSerializerIsGson() {
        SdkConfig config = new SdkConfig();
        assertNotNull(config.getJsonSerializer());
        assertInstanceOf(GsonJsonSerializer.class, config.getJsonSerializer());
        assertSame(GsonJsonSerializer.INSTANCE(), config.getJsonSerializer());
    }

    @Test
    void allGettersAndSetters() {
        JsonSerializer serializer = GsonJsonSerializer.INSTANCE();
        OkHttpClient client = new OkHttpClient();

        SdkConfig config = new SdkConfig()
                .setApiVersion("v61.0")
                .setUsername("user")
                .setPassword("pass")
                .setLoginEndpoint("https://test.salesforce.com")
                .setAuthFlow(AuthFlow.PASSWORD)
                .setClientId("client-id")
                .setClientSecret("client-secret")
                .setAuthorizationCode("auth-code")
                .setRedirectUri("https://app.com/callback")
                .setJsonSerializer(serializer)
                .setCustomObjectNamespace("ns")
                .setDomain("https://domain.my.salesforce.com")
                .setAccessToken("access-token")
                .setDebug(true)
                .setDebugLogBody(true)
                .setDebugBodyMaxSize(2048)
                .setOkHttpClient(client);

        assertEquals("v61.0", config.getApiVersion());
        assertEquals("user", config.getUsername());
        assertEquals("pass", config.getPassword());
        assertEquals("https://test.salesforce.com", config.getLoginEndpoint());
        assertEquals(AuthFlow.PASSWORD, config.getAuthFlow());
        assertEquals("client-id", config.getClientId());
        assertEquals("client-secret", config.getClientSecret());
        assertEquals("auth-code", config.getAuthorizationCode());
        assertEquals("https://app.com/callback", config.getRedirectUri());
        assertSame(serializer, config.getJsonSerializer());
        assertEquals("ns", config.getCustomObjectNamespace());
        assertEquals("ns__", config.getCustomObjectNamespacePrefix());
        assertEquals("__c", config.getCustomObjectSuffix());
        assertEquals("__e", config.getPlatformEventSuffix());
        assertEquals("https://domain.my.salesforce.com", config.getDomain());
        assertEquals("access-token", config.getAccessToken());
        assertTrue(config.isDebug());
        assertTrue(config.isDebugLogBody());
        assertEquals(2048, config.getDebugBodyMaxSize());
        assertSame(client, config.getOkHttpClient());
    }
}
