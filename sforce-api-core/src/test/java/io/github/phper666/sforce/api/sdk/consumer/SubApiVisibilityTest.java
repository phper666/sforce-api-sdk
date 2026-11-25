package io.github.phper666.sforce.api.sdk.consumer;

import io.github.phper666.sforce.api.sdk.BulkApi;
import io.github.phper666.sforce.api.sdk.CompositeApi;
import io.github.phper666.sforce.api.sdk.CustomCodeApi;
import io.github.phper666.sforce.api.sdk.FileApi;
import io.github.phper666.sforce.api.sdk.QueryApi;
import io.github.phper666.sforce.api.sdk.SforceApi;
import io.github.phper666.sforce.api.sdk.SobjectApi;
import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Lives outside {@code io.github.phper666.sforce.api.sdk} on purpose: assigning
 * the sub-APIs to explicitly typed locals only compiles while every sub-API
 * class is public. Guards the external usability of the published SDK.
 */
class SubApiVisibilityTest {

    @Test
    void allSubApisAreAccessibleFromAnotherPackage() {
        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken("test-token")
                .setLoginEndpoint("https://test.salesforce.com");
        SforceApi api = new SforceApi(config);

        SobjectApi sobject = api.sobject();
        QueryApi query = api.query();
        CompositeApi composite = api.composite();
        BulkApi bulk = api.bulk();
        FileApi file = api.file();
        CustomCodeApi customCode = api.customCode();

        assertNotNull(sobject);
        assertNotNull(query);
        assertNotNull(composite);
        assertNotNull(bulk);
        assertNotNull(file);
        assertNotNull(customCode);
    }
}
