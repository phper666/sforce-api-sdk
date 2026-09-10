package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.exception.ApiException;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobRequest;
import io.github.phper666.sforce.api.sdk.model.BulkApiQueryJobResponse;
import io.github.phper666.sforce.api.sdk.model.CompositeRequestBody;
import io.github.phper666.sforce.api.sdk.model.CompositeRequest;
import io.github.phper666.sforce.api.sdk.model.CompositeResponse;
import io.github.phper666.sforce.api.sdk.model.ListInvocableActionResult;
import io.github.phper666.sforce.api.sdk.model.ObjectDescribeResponse;
import io.github.phper666.sforce.api.sdk.model.PageQueryResponse;
import io.github.phper666.sforce.api.sdk.model.QueryJobResult;
import io.github.phper666.sforce.api.sdk.model.SObjectMetadata;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
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
        String objectName = pickQueryableObject();
        ObjectDescribeResponse describe = api.sobject().describe(objectName);
        assertEquals(objectName, describe.getName());
        assertFalse(describe.getFields().isEmpty(), objectName + " should have fields");
        System.out.println("📋 " + objectName + " fields: " + describe.getFields().size());
        describe.getFields().stream()
                .limit(5)
                .forEach(f -> System.out.println("   " + f.getName() + " (" + f.getType() + ")"));
    }

    @Test
    void soqlQuery() {
        String objectName = pickQueryableObject();
        PageQueryResponse<Map> result = api.query().soqlQuery(
                "SELECT Id FROM " + objectName + " LIMIT 5", Map.class);
        assertTrue(result.getTotalSize() > 0, "should find records");
        System.out.println("📋 " + objectName + " found: " + result.getTotalSize());
        result.getRecords().forEach(r ->
                System.out.println("   " + r.get("Id")));
    }

    @Test
    void createAndDeleteRecord() {
        var data = Map.of("LastName", "SDK Test — " + System.currentTimeMillis());
        io.github.phper666.sforce.api.sdk.model.CreateObjectResponse created;
        try {
            created = api.sobject().create("Contact", data);
        } catch (ApiException e) {
            // 环境限制（如 org 存储满）非 SDK 缺陷 → 跳过，避免 live 套件噪音
            Assumptions.abort("skipping create/delete — org rejected write: " + e.getMessage());
            return;
        }
        assertNotNull(created.getId());
        System.out.println("✅ Created Contact: " + created.getId());

        // Update it
        String updatedName = "SDK Updated — " + System.currentTimeMillis();
        api.sobject().update("Contact", created.getId(), Map.of("LastName", updatedName));

        // Verify update landed
        Map<String, Object> reloaded = api.sobject().getSObjectAsMap("Contact", created.getId());
        assertEquals(updatedName, reloaded.get("LastName"), "update must be reflected on re-read");
        System.out.println("✅ Updated + re-read Contact: " + created.getId() + " LastName=" + reloaded.get("LastName"));

        // Delete it
        api.sobject().delete("Contact", created.getId());
        System.out.println("✅ Deleted Contact: " + created.getId());
    }

    @Test
    void batchCreateLive() {
        String suffix = String.valueOf(System.currentTimeMillis());
        List<io.github.phper666.sforce.api.sdk.model.CompositeBodyObject> bodies = new java.util.ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            var b = new io.github.phper666.sforce.api.sdk.model.CompositeBodyObject();
            b.setObjectType("Contact");
            b.setBody(Map.of("LastName", "SDK Batch " + i + " — " + suffix));
            bodies.add(b);
        }
        List<io.github.phper666.sforce.api.sdk.model.CreateObjectResponse> created;
        try {
            created = api.sobject().batchCreateSObjects(bodies);
        } catch (ApiException e) {
            Assumptions.abort("skipping batchCreate — org rejected write: " + e.getMessage());
            return;
        }
        assertEquals(2, created.size());
        // org 存储满时 composite batch 返回 HTTP 200 + 每项 success=false（环境限制非 SDK 缺陷 → abort）
        List<String> failures = created.stream()
                .filter(c -> !c.isSuccess())
                .map(c -> String.valueOf(c.getErrors()))
                .toList();
        if (!failures.isEmpty()) {
            Assumptions.abort("skipping batchCreate — batch item errors: " + failures);
        }
        List<String> ids = created.stream().map(io.github.phper666.sforce.api.sdk.model.CreateObjectResponse::getId).toList();
        System.out.println("✅ batchCreateSObjects created: " + ids);

        // cleanup
        api.sobject().batchDeleteObjects(ids, true);
        System.out.println("✅ Cleaned up batch records: " + ids);
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
        String objectName = pickQueryableObject();
        for (int i = 0; i < 5; i++) {
            api.query().soqlQuery("SELECT Id FROM " + objectName + " LIMIT 1", Map.class);
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
        String objectName = pickQueryableObject();
        var result = fresh.query().soqlQuery("SELECT Id FROM " + objectName + " LIMIT 1", Map.class);
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

    // ── resultPages 并行下载（真实 org，v58.0+）──

    @Test
    void bulkQueryParallelDownload() throws Exception {
        String objectName = pickQueryableObject();
        System.out.println("📋 Bulk parallel query object: " + objectName);

        BulkApiQueryJobRequest request = new BulkApiQueryJobRequest()
                .setObject(objectName)
                .setQuery("SELECT Id FROM " + objectName + " LIMIT 5000");
        BulkApiQueryJobResponse job = api.bulk().createBulkQueryJob(request, null);
        System.out.println("✅ Created bulk query job: " + job.getId() + " state=" + job.getState());

        BulkApiQueryJobResponse completed = api.bulk().waitForJobComplete(job.getId(), 1000L, 120000L, null);
        System.out.println("📊 JobComplete numberRecordsProcessed=" + completed.getNumberRecordsProcessed());

        File dst = File.createTempFile("bulk-parallel", ".csv");
        dst.deleteOnExit();
        // resultPages 并行下载（并发 3）；分段由服务端决定（v58.0+）
        api.bulk().downloadBulkQueryJobResultParallel(job.getId(), dst, 3, null);

        List<String> lines = Files.readAllLines(dst.toPath());
        assertFalse(lines.isEmpty(), "result file should contain at least the header");

        // 并行拼接后表头必须恰好出现一次（每段可能各带表头，需去重）
        String header = lines.get(0);
        long headerCount = lines.stream().filter(header::equals).count();
        assertEquals(1, headerCount, "CSV header must appear exactly once after parallel merge");

        long dataRows = lines.size() - 1L;
        System.out.println("✅ Parallel download rows=" + dataRows + " header=" + header);
        assertEquals(completed.getNumberRecordsProcessed(), (int) dataRows,
                "downloaded rows must match job numberRecordsProcessed");

        // cleanup
        api.bulk().deleteBulkQueryJob(job.getId(), null);
    }

    // ── QueryApi 扩展（真实 org）──

    @Test
    void soqlQueryCountLive() {
        String objectName = pickQueryableObject();
        int count = api.query().soqlQueryCount("SELECT COUNT() FROM " + objectName);
        assertTrue(count >= 0, "COUNT() must return a non-negative total");
        System.out.println("✅ soqlQueryCount " + objectName + " = " + count);
    }

    @Test
    void soqlQueryAllAggregatesPages() {
        String objectName = pickQueryableObject();
        PageQueryResponse<Map> all = api.query().soqlQueryAll(
                "SELECT Id FROM " + objectName + " LIMIT 5000", Map.class);
        assertTrue(all.getDone(), "aggregated result must be marked done");
        assertEquals(all.getRecords().size(), all.getTotalSize(),
                "aggregated records size must equal totalSize");
        System.out.println("✅ soqlQueryAll aggregated records=" + all.getTotalSize());
    }

    @Test
    void soqlQueryNextManualPaging() {
        String objectName = pickQueryableObject();
        PageQueryResponse<Map> page1 = api.query().soqlQuery("SELECT Id FROM " + objectName, Map.class);
        if (page1.getNextRecordsUrl() == null) {
            System.out.println("⏭️ " + objectName + " fits in one page (" + page1.getTotalSize()
                    + " rows) — no nextRecordsUrl to follow, skipping");
            return;
        }
        PageQueryResponse<Map> page2 = api.query().soqlQueryNext(page1.getNextRecordsUrl(), Map.class);
        assertFalse(page2.getRecords().isEmpty(), "second page must return records");
        System.out.println("✅ soqlQueryNext page1=" + page1.getRecords().size()
                + " page2=" + page2.getRecords().size() + " total=" + page2.getTotalSize());
    }

    @Test
    void soslQueryLive() {
        // 只验证链路 + 响应可解析（可能 0 结果）
        var result = api.query().soslQuery("FIND {test} IN ALL FIELDS RETURNING Contact(Id)");
        assertNotNull(result, "SOSL response must be parseable");
        int hits = result.getSearchRecords() == null ? 0 : result.getSearchRecords().size();
        System.out.println("✅ soslQuery searchRecords=" + hits);
    }

    @Test
    void toolingApiQueryLive() {
        try {
            PageQueryResponse<Map> result = api.query().toolingApiSoqlQuery(
                    "SELECT Id FROM ApexClass LIMIT 1", Map.class);
            assertNotNull(result);
            System.out.println("✅ toolingApiSoqlQuery totalSize=" + result.getTotalSize());
        } catch (ApiException e) {
            // tooling API 需要额外权限；部分 org/integration user 连 ApexClass 元数据都不可见
            // （400 INVALID_TYPE），非 SDK 缺陷 → 换 tooling 内置对象再试，仍失败则跳过
            if (e.getCode() == 401 || e.getCode() == 403) {
                Assumptions.abort("skipping tooling API — no permission: " + e.getMessage());
            }
            if (e.getCode() == 400) {
                try {
                    PageQueryResponse<Map> fallback = api.query().toolingApiSoqlQuery(
                            "SELECT DurableId FROM EntityDefinition LIMIT 1", Map.class);
                    assertNotNull(fallback);
                    System.out.println("✅ toolingApiSoqlQuery (EntityDefinition fallback) totalSize=" + fallback.getTotalSize());
                    return;
                } catch (ApiException e2) {
                    Assumptions.abort("skipping tooling API — tooling objects not accessible in org: " + e2.getMessage());
                }
            }
            throw e;
        }
    }

    // ── SobjectApi 读路径 ──

    @Test
    void getSObjectByIdLive() {
        String objectName = pickQueryableObject();
        PageQueryResponse<Map> one = api.query().soqlQuery(
                "SELECT Id FROM " + objectName + " LIMIT 1", Map.class);
        Assumptions.assumeFalse(one.getRecords().isEmpty(), "no data rows to read");
        String id = (String) one.getRecords().get(0).get("Id");

        Map<String, Object> record = api.sobject().getSObjectAsMap(objectName, id);
        assertEquals(id, record.get("Id"), "retrieved record Id must match the queried Id");
        System.out.println("✅ getSObjectAsMap " + objectName + "/" + id + " fields=" + record.size());
    }

    @Test
    void batchGetSObjectsLive() {
        String objectName = pickQueryableObject();
        PageQueryResponse<Map> two = api.query().soqlQuery(
                "SELECT Id FROM " + objectName + " LIMIT 2", Map.class);
        Assumptions.assumeFalse(two.getRecords().isEmpty(), "no data rows to read");
        List<String> ids = two.getRecords().stream().map(r -> (String) r.get("Id")).toList();

        List<Map> records = api.sobject().batchGetSObjects(objectName, ids, List.of("Id"), Map.class);
        assertEquals(ids.size(), records.size(), "batchGet must return one record per id");
        System.out.println("✅ batchGetSObjects ids=" + ids.size() + " returned=" + records.size());
    }

    // ── Composite API ──

    @Test
    void compositeReadRequestsLive() {
        String objectName = pickQueryableObject();
        PageQueryResponse<Map> one = api.query().soqlQuery(
                "SELECT Id FROM " + objectName + " LIMIT 1", Map.class);
        Assumptions.assumeFalse(one.getRecords().isEmpty(), "no data rows to read");
        String id = (String) one.getRecords().get(0).get("Id");
        String url = api.composite().getCompositeSObjectUrl(objectName) + "/" + id;

        CompositeRequest r1 = new CompositeRequest();
        r1.setMethod("GET");
        r1.setUrl(url);
        r1.setReferenceId("ref1");
        CompositeRequest r2 = new CompositeRequest();
        r2.setMethod("GET");
        r2.setUrl(url);
        r2.setReferenceId("ref2");

        CompositeRequestBody body = new CompositeRequestBody();
        body.setCompositeRequest(List.of(r1, r2));

        var resp = api.composite().compositeRequest(body);
        assertEquals(2, resp.getCompositeResponse().size(), "must return one response per subrequest");
        for (CompositeResponse cr : resp.getCompositeResponse()) {
            assertTrue(cr.isSuccessful(), "subrequest " + cr.getReferenceId()
                    + " failed: " + cr.getHttpStatusCode() + " " + cr.getCompositeResponseErrors());
        }
        System.out.println("✅ compositeRequest 2 GET subrequests OK for " + id);
    }

    // ── Bulk API 2.0 补全缺口 ──

    private static BulkApiQueryJobResponse createAndAwaitBulkJob(String objectName, String query, BulkApi.JobOperation operation) {
        BulkApiQueryJobRequest request = new BulkApiQueryJobRequest()
                .setObject(objectName)
                .setQuery(query)
                .setOperation(operation);
        BulkApiQueryJobResponse job = api.bulk().createBulkQueryJob(request, null);
        System.out.println("✅ Created bulk query job: " + job.getId() + " op=" + operation);
        BulkApiQueryJobResponse completed = api.bulk().waitForJobComplete(job.getId(), 1000L, 120000L, null);
        System.out.println("📊 JobComplete numberRecordsProcessed=" + completed.getNumberRecordsProcessed());
        return completed;
    }

    @Test
    void bulkSerialDownloadToFile() throws Exception {
        String objectName = pickQueryableObject();
        BulkApiQueryJobResponse completed = createAndAwaitBulkJob(
                objectName, "SELECT Id FROM " + objectName + " LIMIT 5000", BulkApi.JobOperation.QUERY);
        try {
            File dst = File.createTempFile("bulk-serial", ".csv");
            dst.deleteOnExit();
            // maxRecords=1000 强制 locator 多页分页（Contact 3376 行 → 至少 4 页）
            api.bulk().downloadBulkQueryJobResult(completed.getId(), dst, 1000, null);

            List<String> lines = Files.readAllLines(dst.toPath());
            long dataRows = lines.size() - 1L;
            assertTrue(dataRows > 2000, "expected multi-page aggregated download (rows=" + dataRows + ")");
            assertEquals(completed.getNumberRecordsProcessed(), (int) dataRows,
                    "downloaded rows must match job numberRecordsProcessed");
            System.out.println("✅ Serial multi-page download rows=" + dataRows);
        } finally {
            api.bulk().deleteBulkQueryJob(completed.getId(), null);
        }
    }

    @Test
    void bulkForEachResultRowLive() {
        String objectName = pickQueryableObject();
        BulkApiQueryJobResponse completed = createAndAwaitBulkJob(
                objectName, "SELECT Id FROM " + objectName + " LIMIT 5000", BulkApi.JobOperation.QUERY);
        try {
            AtomicInteger count = new AtomicInteger();
            api.bulk().forEachResultRow(completed.getId(), 1000, row -> count.incrementAndGet(), null);
            assertEquals(completed.getNumberRecordsProcessed(), count.get(),
                    "row callback must be invoked once per record");
            System.out.println("✅ forEachResultRow rows=" + count.get());
        } finally {
            api.bulk().deleteBulkQueryJob(completed.getId(), null);
        }
    }

    @Test
    void bulkForEachResultPageParallelLive() {
        String objectName = pickQueryableObject();
        BulkApiQueryJobResponse completed = createAndAwaitBulkJob(
                objectName, "SELECT Id FROM " + objectName + " LIMIT 5000", BulkApi.JobOperation.QUERY);
        try {
            AtomicInteger rows = new AtomicInteger();
            AtomicInteger pages = new AtomicInteger();
            api.bulk().forEachResultPageParallel(completed.getId(), 3, page -> {
                pages.incrementAndGet();
                rows.addAndGet(page.size());
            }, null);
            assertEquals(completed.getNumberRecordsProcessed(), rows.get(),
                    "parallel page consumption must cover all records");
            System.out.println("✅ forEachResultPageParallel pages=" + pages.get() + " rows=" + rows.get());
        } finally {
            api.bulk().deleteBulkQueryJob(completed.getId(), null);
        }
    }

    @Test
    void bulkRunQueryJobsLive() throws Exception {
        String objectName = pickQueryableObject();
        File dstDir = Files.createTempDirectory("bulk-run-jobs").toFile();
        // 两条完全相同的 SOQL：修复后各产出独立结果（各带自己的 job id 与文件）
        List<String> queries = List.of(
                "SELECT Id FROM " + objectName + " LIMIT 100",
                "SELECT Id FROM " + objectName + " LIMIT 100");

        List<QueryJobResult> results = api.bulk().runQueryJobs(queries, objectName, dstDir, 2, 1000, null);

        assertEquals(2, results.size(), "one result entry per query (duplicates preserved)");
        assertEquals(queries.get(0), results.get(0).query());
        assertEquals(queries.get(1), results.get(1).query());
        assertNotEquals(results.get(0).jobId(), results.get(1).jobId(), "each duplicate gets its own job");
        for (QueryJobResult r : results) {
            assertTrue(r.resultFile().exists() && r.resultFile().length() > 0,
                    "result file must be non-empty for: " + r.query());
            System.out.println("✅ runQueryJobs file=" + r.resultFile().getName()
                    + " bytes=" + r.resultFile().length() + " jobId=" + r.jobId());
        }
        // 现在能拿到 jobId → 显式清理本次产生的 job
        for (QueryJobResult r : results) {
            api.bulk().deleteBulkQueryJob(r.jobId(), null);
        }
    }

    @Test
    void bulkQueryAllOperationLive() {
        String objectName = pickQueryableObject();
        BulkApiQueryJobResponse completed = createAndAwaitBulkJob(
                objectName, "SELECT Id FROM " + objectName, BulkApi.JobOperation.QUERY_ALL);
        try {
            assertTrue(completed.getNumberRecordsProcessed() >= 0,
                    "QUERY_ALL job must complete with a non-negative record count (deleted rows may be 0)");
            System.out.println("✅ QUERY_ALL numberRecordsProcessed=" + completed.getNumberRecordsProcessed());
        } finally {
            api.bulk().deleteBulkQueryJob(completed.getId(), null);
        }
    }

    // ── CustomCodeApi ──

    @Test
    void listStandardInvocableActionsLive() {
        ListInvocableActionResult result;
        try {
            result = api.customCode().listStandardInvocableActions();
        } catch (ApiException e) {
            // invocable actions 元数据接口可能未授权/未启用 — 非 SDK 缺陷 → 跳过
            if (e.getCode() == 401 || e.getCode() == 403 || e.getCode() == 404) {
                Assumptions.abort("skipping invocable actions — not permitted/enabled in org: " + e.getMessage());
            }
            throw e;
        }
        assertNotNull(result, "response must deserialize");
        int count = result.getActions() == null ? 0 : result.getActions().size();
        System.out.println("✅ listStandardInvocableActions count=" + count);
    }

    // ── Error handling ──

    @Test
    void invalidSoqlThrowsApiException() {
        ApiException e = assertThrows(ApiException.class, () -> api.query().soqlQuery(
                "SELECT BadField__c FROM NoSuchObject", Map.class));
        System.out.println("✅ Invalid SOQL → ApiException code=" + e.getCode() + " " + e.getMessage());
    }

    // ── datahub-app 认证模式：ACCESS_TOKEN flow + 自定义 OkHttpClient ──

    @Test
    void accessTokenFlowWithCustomClientLive() {
        // datahub 模式：自己换 token → ACCESS_TOKEN flow + 自定义 hardened client 注入
        String token = api.getAccessToken();
        String endpoint = api.getApiEndpoint();

        AtomicInteger customClientCalls = new AtomicInteger();
        OkHttpClient customClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    customClientCalls.incrementAndGet();
                    return chain.proceed(chain.request().newBuilder()
                            .header("X-Custom-Client", "datahub-live-test").build());
                })
                .build();

        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(token)
                .setLoginEndpoint(endpoint)
                .setOkHttpClient(customClient);
        SforceApi tokenApi = new SforceApi(config);

        String objectName = pickQueryableObject();
        var result = tokenApi.query().soqlQuery("SELECT Id FROM " + objectName + " LIMIT 1", Map.class);

        assertTrue(result.getTotalSize() >= 0, "ACCESS_TOKEN flow query must succeed");
        assertEquals(token, tokenApi.getAccessToken(), "ACCESS_TOKEN flow must reuse the injected token");
        assertTrue(customClientCalls.get() > 0, "custom OkHttpClient must be used for the request");
        System.out.println("✅ ACCESS_TOKEN flow + custom client: object=" + objectName
                + " totalSize=" + result.getTotalSize()
                + " customClientCalls=" + customClientCalls.get());
    }
}
