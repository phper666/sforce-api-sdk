package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.exception.ApiException;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobResponse;
import io.github.phper666.sforce.api.sdk.model.ObjectDescribeResponse;
import io.github.phper666.sforce.api.sdk.model.PageQueryResponse;
import io.github.phper666.sforce.api.sdk.model.SObjectMetadata;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
        assertFalse(describes.isEmpty(), "should return at least some describes");
        describes.forEach(d ->
                System.out.println("📋 " + d.getName() + " — " + d.getFields().size() + " fields"));
    }

    @Test
    void tokenIsReusedAcrossRequests() throws Exception {
        String token1 = api.getAccessToken();
        assertNotNull(token1);
        // 连续多次查询 — 复用一个 SforceApi 实例，token 不应变化（未重复 login）
        for (int i = 0; i < 5; i++) {
            api.query().soqlQuery("SELECT Id FROM Account LIMIT 1", Map.class);
        }
        String token2 = api.getAccessToken();
        assertEquals(token1, token2, "token should be cached and reused, not re-fetched per request");
        System.out.println("✅ Token reused across 5 requests: " + token1.substring(0, 20) + "...");
    }

    @Test
    void newInstanceCreatesWorkingApi() {
        // 新实例 = 新 SforceApi，独立 login。Salesforce 对相同 client_credential
        // 在短期窗口内可能返回相同 token（服务端缓存），所以不比较 token 差异，
        // 只验证新实例能独立工作。
        var config = new SdkConfig()
                .setAuthFlow(AuthFlow.CLIENT_CREDENTIAL)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setLoginEndpoint(INSTANCE_URL)
                .setDebug(false);
        SforceApi fresh = new SforceApi(config);
        assertNotNull(fresh.getAccessToken());
        var result = fresh.query().soqlQuery("SELECT Id FROM Account LIMIT 1", Map.class);
        assertTrue(result.getTotalSize() >= 0, "new instance should query successfully");
        System.out.println("✅ New instance works independently: " + fresh.getAccessToken().substring(0, 20) + "...");
    }

    // ── Bulk API 2.0 Query（真实 org）──

    private static String pickQueryableObject() {
        // 优先选择真实有数据的标准对象（Contact），确保验证到实际数据行；
        // 回退到任意可查询对象（可能 0 行，仅验证链路）。
        String preferred = api.sobject().listObjects().stream()
                .filter(SObjectMetadata::isQueryable)
                .map(SObjectMetadata::getName)
                .filter("Contact"::equals)
                .findFirst()
                .orElse(null);
        if (preferred != null) {
            return preferred;
        }
        return api.sobject().listObjects().stream()
                .filter(SObjectMetadata::isQueryable)
                .filter(o -> !o.isCustom())
                .map(SObjectMetadata::getName)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no queryable object found in this org"));
    }

    @Test
    void bulkQueryForEachResultPage() {
        String objectName = pickQueryableObject();
        System.out.println("📋 Bulk query object: " + objectName);

        BulkApiQueryJobRequest request = new BulkApiQueryJobRequest()
                .setObject(objectName)
                .setQuery("SELECT Id FROM " + objectName + " LIMIT 5000");
        BulkApiQueryJobResponse job = api.bulk().createBulkQueryJob(request, null);
        System.out.println("✅ Created bulk query job: " + job.getId() + " state=" + job.getState());

        // 先取 JobComplete 态的 numberRecordsProcessed 作为核对基准（创建响应里该字段为 null）
        BulkApiQueryJobResponse completed = api.bulk().waitForJobComplete(job.getId(), 1000L, 120000L, null);
        System.out.println("📊 JobComplete numberRecordsProcessed=" + completed.getNumberRecordsProcessed());

        AtomicInteger pageCount = new AtomicInteger();
        AtomicInteger rowCount = new AtomicInteger();
        // maxRecords=1000 强制多页分页，验证 locator 翻页链路
        api.bulk().forEachResultPage(job.getId(), 1000, 1000L, 120000L,
                page -> {
                    pageCount.incrementAndGet();
                    rowCount.addAndGet(page.size());
                },
                null, true);

        System.out.println("✅ forEachResultPage pages=" + pageCount.get() + " rows=" + rowCount.get());
        assertTrue(rowCount.get() > 0, "should receive real data rows");
        assertEquals(completed.getNumberRecordsProcessed(), rowCount.get(),
                "received rows must match job numberRecordsProcessed");

        // deleteOnComplete=true → job 应已被删除，getBulkQueryJob 应报 404
        assertThrows(ApiException.class, () -> api.bulk().getBulkQueryJob(job.getId(), null));
    }

    @Test
    void bulkQueryIterator() {
        String objectName = pickQueryableObject();

        BulkApiQueryJobRequest request = new BulkApiQueryJobRequest()
                .setObject(objectName)
                .setQuery("SELECT Id FROM " + objectName + " LIMIT 5000");
        BulkApiQueryJobResponse job = api.bulk().createBulkQueryJob(request, null);
        System.out.println("✅ Created bulk query job: " + job.getId() + " state=" + job.getState());

        // 先取 JobComplete 态的 numberRecordsProcessed 作为核对基准（创建响应里该字段为 null）
        BulkApiQueryJobResponse completed = api.bulk().waitForJobComplete(job.getId(), 1000L, 120000L, null);
        System.out.println("📊 JobComplete numberRecordsProcessed=" + completed.getNumberRecordsProcessed());

        int rows = 0;
        int pages = 0;
        try (BulkApi.QueryResultIterator it = api.bulk()
                .queryResultIterator(job.getId(), 1000, 1000L, 120000L, null, true)) {
            while (it.hasNext()) {
                pages++;
                rows += it.next().size();
            }
        }

        System.out.println("✅ QueryResultIterator pages=" + pages + " rows=" + rows);
        assertTrue(rows > 0, "should receive real data rows");
        assertEquals(completed.getNumberRecordsProcessed(), rows,
                "received rows must match job numberRecordsProcessed");

        // close() 且完整消费 → deleteOnComplete=true → job 应已删除
        assertThrows(ApiException.class, () -> api.bulk().getBulkQueryJob(job.getId(), null));
    }
}
