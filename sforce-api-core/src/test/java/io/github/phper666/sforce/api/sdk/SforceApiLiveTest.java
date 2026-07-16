package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.model.ObjectDescribeResponse;
import io.github.phper666.sforce.api.sdk.model.PageQueryResponse;
import io.github.phper666.sforce.api.sdk.model.SObjectMetadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live integration test against a real Salesforce org.
 * <p>
 * Reads credentials from environment variables or system properties:
 * <ul>
 *   <li>SF_INSTANCE_URL or sf.instance.url — e.g. {@code https://xxx.my.salesforce.com}</li>
 *   <li>SF_CLIENT_ID or sf.client.id — Consumer Key</li>
 *   <li>SF_CLIENT_SECRET or sf.client.secret — Consumer Secret</li>
 * </ul>
 * Disabled by default. Run with:
 * {@code mvn test -pl sforce-api-core -Dtest=SforceApiLiveTest -DfailIfNoTests=false}
 */
@Disabled("Requires real Salesforce credentials — set environment variables before enabling")
public class SforceApiLiveTest {

    private static final String INSTANCE_URL  = env("SF_INSTANCE_URL",  "sf.instance.url");
    private static final String CLIENT_ID     = env("SF_CLIENT_ID",     "sf.client.id");
    private static final String CLIENT_SECRET = env("SF_CLIENT_SECRET", "sf.client.secret");

    private static String env(String envName, String propName) {
        String val = System.getenv(envName);
        if (val == null || val.isBlank()) {
            val = System.getProperty(propName);
        }
        return val;
    }

    private static SforceApi api;

    @BeforeAll
    static void setup() {
        if (INSTANCE_URL == null || CLIENT_ID == null || CLIENT_SECRET == null) {
            throw new IllegalStateException(
                    "Set SF_INSTANCE_URL, SF_CLIENT_ID, SF_CLIENT_SECRET environment variables");
        }
        var config = new SdkConfig()
                .setAuthFlow(AuthFlow.CLIENT_CREDENTIAL)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setLoginEndpoint(INSTANCE_URL)
                .setDebug(true);

        api = new SforceApi(config);
    }

    @Test
    void connectionWorks() {
        assertNotNull(api.getAccessToken(), "access token should not be null");
        assertTrue(api.getAccessToken().length() > 20, "access token looks valid");
        System.out.println("✅ Connected! Token: " + api.getAccessToken().substring(0, 20) + "...");
    }

    @Test
    void listObjects() {
        List<SObjectMetadata> objects = api.sobject().listObjects();
        assertFalse(objects.isEmpty(), "should return at least some objects");
        System.out.println("📋 Total objects: " + objects.size());
        objects.stream()
                .filter(o -> !o.isCustom())
                .limit(400)
                .forEach(o -> System.out.println("   " + o.getName() + " — " + o.getLabel()));
    }

    @Test
    void describeStandardObject() {
        ObjectDescribeResponse describe = api.sobject().describe("Account");
        assertEquals("Account", describe.getName());
        assertFalse(describe.getFields().isEmpty(), "Account should have fields");
        System.out.println("📋 Account fields: " + describe.getFields().size());
        describe.getFields().stream()
                .limit(5)
                .forEach(f -> System.out.println("   " + f.getName() + " (" + f.getType() + ")"));
    }

    @Test
    void soqlQuery() {
        PageQueryResponse<Map> result = api.query().soqlQuery(
                "SELECT Id, Name FROM Account LIMIT 5", Map.class);
        assertTrue(result.getTotalSize() > 0, "should find accounts");
        System.out.println("📋 Accounts found: " + result.getTotalSize());
        result.getRecords().forEach(r ->
                System.out.println("   " + r.get("Id") + " — " + r.get("Name")));
    }

    @Test
    void createAndDeleteAccount() {
        var data = Map.of("Name", "SDK Test Account — " + System.currentTimeMillis());
        var created = api.sobject().create("Account", data);
        assertNotNull(created.getId());
        System.out.println("✅ Created Account: " + created.getId());

        // Delete it
        api.sobject().delete("Account", created.getId());
        System.out.println("✅ Deleted Account: " + created.getId());
    }

    @Test
    void describeMultipleObjects() {
        var describes = api.sobject().describeObjects(List.of("Account", "Contact", "Opportunity"));
        assertEquals(3, describes.size());
        describes.forEach(d ->
                System.out.println("📋 " + d.getName() + " — " + d.getFields().size() + " fields"));
    }
}
