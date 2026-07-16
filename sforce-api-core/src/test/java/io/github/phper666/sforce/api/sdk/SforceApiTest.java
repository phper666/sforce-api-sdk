package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.AuthFlow;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;
import io.github.phper666.sforce.api.sdk.exception.ApiException;
import io.github.phper666.sforce.api.sdk.internal.BaseApi;
import io.github.phper666.sforce.api.sdk.model.*;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SforceApiTest {

    private static final String DOMAIN = "https://testinstance.salesforce.com";
    private static final String ACCESS_TOKEN = "test-access-token";

    private static Response buildResponse(Request request, int code, String body) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code == 401 ? "Unauthorized" : "OK")
                .body(ResponseBody.create(MediaType.parse("application/json"), body))
                .build();
    }

    private static SforceApi apiWith(Interceptor interceptor) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(ACCESS_TOKEN)
                .setLoginEndpoint(DOMAIN)
                .setOkHttpClient(client);
        return new SforceApi(config);
    }

    @Test
    void accessorsReturnSessionValues() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200, "{}"));
        assertEquals(ACCESS_TOKEN, api.getAccessToken());
        assertEquals(DOMAIN, api.getApiEndpoint());
    }

    @Test
    void buildRequestIncludesAuthorizationHeader() {
        AtomicReference<Request> captured = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            captured.set(chain.request());
            return buildResponse(chain.request(), 200, "{}");
        });

        api.sobject().getSObjectAsMap("Account", "001xx000003DGbA");

        Request request = captured.get();
        assertNotNull(request);
        assertEquals("Bearer " + ACCESS_TOKEN, request.header("Authorization"));
    }

    @Test
    void executeRetriesOn401AndReturnsResult() {
        AtomicInteger counter = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            int count = counter.incrementAndGet();
            if (count == 1) {
                return buildResponse(chain.request(), 401, "");
            }
            return buildResponse(chain.request(), 200, "{\"Id\":\"001xx000003DGbA\",\"Name\":\"Test\"}");
        });

        Map<String, Object> result = api.sobject().getSObjectAsMap("Account", "001xx000003DGbA");
        assertEquals("001xx000003DGbA", result.get("Id"));
        assertEquals("Test", result.get("Name"));
        assertEquals(2, counter.get());
    }

    @Test
    void executeThrowsApiExceptionOnFailure() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 400, "{\"error\":\"bad request\"}"));
        ApiException exception = assertThrows(ApiException.class,
                () -> api.sobject().getSObjectAsMap("Account", "001xx000003DGbA"));
        assertEquals(400, exception.getCode());
        assertEquals("GET", exception.getMethod());
    }

    @Test
    void createParsesResponse() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"id\":\"001xx000003DGbA\",\"success\":true}"));

        CreateObjectResponse response = api.sobject().create("Account", Map.of("Name", "Test"));
        assertEquals("001xx000003DGbA", response.getId());
        assertTrue(response.isSuccess());
    }

    @Test
    void createAndGetId() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"id\":\"001xx000003DGbA\",\"success\":true}"));
        assertEquals("001xx000003DGbA", api.sobject().create("Account", Map.of("Name", "Test")).getId());
    }

    @Test
    void deleteDoesNotThrow() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 204, ""));
        assertDoesNotThrow(() -> api.sobject().delete("Account", "001xx000003DGbA"));
    }

    @Test
    void updateDoesNotThrow() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200, ""));
        assertDoesNotThrow(() -> api.sobject().update("Account", "001xx000003DGbA", Map.of("Name", "Updated")));
    }

    @Test
    void upsertParsesResponse() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"id\":\"001xx000003DGbA\",\"success\":true,\"created\":true}"));

        CreateOrUpdateObjectResponse response = api.sobject().upsert(
                "Account", "External_Id__c", "ext-1", Map.of("Name", "Test"));
        assertEquals("001xx000003DGbA", response.getId());
        assertTrue(response.isSuccess());
        assertTrue(response.isCreated());
    }

    @Test
    void soqlQueryParsesResponse() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"totalSize\":1,\"done\":true,\"records\":[{\"Name\":\"Test\"}],\"nextRecordsUrl\":null}"));

        PageQueryResponse<Map> response = api.query().soqlQuery("SELECT Id FROM Account", Map.class);
        assertEquals(1, response.getTotalSize());
        assertTrue(response.getDone());
        assertNotNull(response.getRecords());
        assertEquals(1, response.getRecords().size());
        assertEquals("Test", response.getRecords().get(0).get("Name"));
    }

    @Test
    void soqlQueryCountReturnsTotalSize() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"totalSize\":42,\"done\":true}"));
        assertEquals(42, api.query().soqlQueryCount("SELECT COUNT() FROM Account"));
    }

    @Test
    void describeObjectReturnsRawBody() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"name\":\"Account\",\"label\":\"Account\"}"));
        String body = api.sobject().describeObject("Account");
        assertTrue(body.contains("\"name\":\"Account\""));
    }

    @Test
    void describeParsesResponse() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"name\":\"Account\",\"label\":\"Account\",\"queryable\":true,\"fields\":[{\"name\":\"Id\",\"type\":\"id\"}]}"));

        ObjectDescribeResponse response = api.sobject().describe("Account");
        assertEquals("Account", response.getName());
        assertEquals(1, response.getFieldNames().size());
        assertEquals("Id", response.getFieldNames().get(0));
    }

    @Test
    void getUserInfoDeserializesResponse() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"user_id\":\"005xx000001X9qAAAS\",\"organization_id\":\"00Dxx0000001gEgEAI\"}"));

        Map<String, Object> userInfo = api.getUserInfo(Map.class);
        assertEquals("005xx000001X9qAAAS", userInfo.get("user_id"));
    }

    @Test
    void compositeRequestParsesResponse() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"compositeResponse\":[{\"body\":{\"Id\":\"001xx000003DGbA\"},\"httpHeaders\":{},\"httpStatusCode\":200,\"referenceId\":\"ref1\"}]}"));

        CompositeRequest request = new CompositeRequest();
        request.setMethod(HttpMethod.GET.name());
        request.setUrl("/services/data/v62.0/sobjects/Account/001xx000003DGbA");
        request.setReferenceId("ref1");

        CompositeRequestBody body = new CompositeRequestBody();
        body.setCompositeRequest(Collections.singletonList(request));

        CompositeResponseBody response = api.composite().compositeRequest(body);
        assertNotNull(response.getCompositeResponse());
        assertEquals(1, response.getCompositeResponse().size());
        assertEquals(200, response.getCompositeResponse().get(0).getHttpStatusCode());
        assertTrue(response.getCompositeResponse().get(0).isSuccessful());
    }

    @Test
    void createAppliesNamespaceAndSuffix() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    capturedPath.set(chain.request().url().encodedPath());
                    return buildResponse(chain.request(), 200, "{\"id\":\"a00xx0000000001\",\"success\":true}");
                })
                .build();

        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(ACCESS_TOKEN)
                .setLoginEndpoint(DOMAIN)
                .setCustomObjectNamespace("myns")
                .setOkHttpClient(client);

        SforceApi api = new SforceApi(config);
        String id = api.sobject().create("CustomObj__c", Map.of("Name", "Test")).getId();
        assertEquals("a00xx0000000001", id);
        assertNotNull(capturedPath.get());
        assertTrue(capturedPath.get().contains("myns__CustomObj__c"));
    }

    @Test
    void runApexReturnsResponseBody() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200, "\"hello\""));
        String result = api.customCode().runApex("/myService", HttpMethod.GET, null);
        assertEquals("\"hello\"", result);
    }

    @Test
    void soslQueryParsesResponse() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"searchRecords\":[{\"Id\":\"001xx000003DGbA\"}],\"metadata\":null}"));

        SOSLQueryResponse response = api.query().soslQuery("FIND {Test}");
        assertNotNull(response.getSearchRecords());
        assertEquals(1, response.getSearchRecords().size());
    }

    @Test
    void describeUsesCustomObjectType() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    capturedPath.set(chain.request().url().encodedPath());
                    return buildResponse(chain.request(), 200,
                            "{\"name\":\"myns__CustomObj__c\",\"label\":\"CustomObj\"}");
                })
                .build();

        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(ACCESS_TOKEN)
                .setLoginEndpoint(DOMAIN)
                .setCustomObjectNamespace("myns")
                .setOkHttpClient(client);

        SforceApi api = new SforceApi(config);
        ObjectDescribeResponse response = api.sobject().describe("CustomObj__c");
        assertEquals("myns__CustomObj__c", response.getName());
        assertTrue(capturedPath.get().contains("myns__CustomObj__c/describe"));
    }

    @Test
    void getByExternalIdEncodesValue() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200, "{\"Id\":\"001xx\"}"));
        Map<String, Object> result = api.sobject().getByExternalId(
                "Account", "External_Id__c", "a/b", Map.class);
        assertEquals("001xx", result.get("Id"));
    }

    @Test
    void unifiedCreateResolvesCustomObjectType() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    capturedPath.set(chain.request().url().encodedPath());
                    return buildResponse(chain.request(), 200, "{\"id\":\"a00xx0000000001\",\"success\":true}");
                })
                .build();

        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(ACCESS_TOKEN)
                .setLoginEndpoint(DOMAIN)
                .setCustomObjectNamespace("myns")
                .setOkHttpClient(client);

        SforceApi api = new SforceApi(config);
        CreateObjectResponse response = api.sobject().create("MyObj__c", Map.of("Name", "Test"));
        assertEquals("a00xx0000000001", response.getId());
        assertTrue(capturedPath.get().contains("myns__MyObj__c"));
    }

    @Test
    void unifiedGetReturnsRecord() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"Id\":\"001xx000003DGbA\",\"Name\":\"Test\"}"));
        Map<String, Object> result = api.sobject().get("Account", "001xx000003DGbA", Map.class);
        assertEquals("001xx000003DGbA", result.get("Id"));
        assertEquals("Test", result.get("Name"));
    }

    @Test
    void unifiedUpdateResolvesCustomObjectType() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    capturedPath.set(chain.request().url().encodedPath());
                    return buildResponse(chain.request(), 200, "");
                })
                .build();

        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(ACCESS_TOKEN)
                .setLoginEndpoint(DOMAIN)
                .setCustomObjectNamespace("myns")
                .setOkHttpClient(client);

        SforceApi api = new SforceApi(config);
        assertDoesNotThrow(() -> api.sobject().update("MyObj__c", "a00xx0000000001", Map.of("Name", "Updated")));
        assertTrue(capturedPath.get().contains("myns__MyObj__c"));
    }

    @Test
    void unifiedDeleteDoesNotThrow() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 204, ""));
        assertDoesNotThrow(() -> api.sobject().delete("Account", "001xx000003DGbA"));
    }

    @Test
    void unifiedUpsertParsesResponse() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"id\":\"001xx000003DGbA\",\"success\":true,\"created\":true}"));
        CreateOrUpdateObjectResponse response = api.sobject().upsert(
                "Account", "External_Id__c", "ext-1", Map.of("Name", "Test"));
        assertEquals("001xx000003DGbA", response.getId());
        assertTrue(response.isSuccess());
        assertTrue(response.isCreated());
    }

    @Test
    void unifiedGetByExternalIdEncodesValue() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200, "{\"Id\":\"001xx\"}"));
        Map<String, Object> result = api.sobject().getByExternalId(
                "Account", "External_Id__c", "a/b", Map.class);
        assertEquals("001xx", result.get("Id"));
    }

    @Test
    void unifiedDescribeParsesResponse() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"name\":\"Account\",\"label\":\"Account\",\"queryable\":true,\"fields\":[{\"name\":\"Id\",\"type\":\"id\"}]}"));
        ObjectDescribeResponse response = api.sobject().describe("Account");
        assertEquals("Account", response.getName());
        assertEquals(1, response.getFieldNames().size());
        assertEquals("Id", response.getFieldNames().get(0));
    }

    @Test
    void resolveTypeAppliesNamespaceForCustomObjectsAndEvents() throws Exception {
        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(ACCESS_TOKEN)
                .setLoginEndpoint(DOMAIN)
                .setCustomObjectNamespace("myns");
        SforceApi api = new SforceApi(config);

        Method resolveType = BaseApi.class.getDeclaredMethod("resolveType", String.class);
        resolveType.setAccessible(true);

        assertEquals("Account", resolveType.invoke(api.sobject(), "Account"));
        assertEquals("myns__MyObj__c", resolveType.invoke(api.sobject(), "MyObj__c"));
        assertEquals("myns__MyEvent__e", resolveType.invoke(api.sobject(), "MyEvent__e"));

        // Already-qualified names — no double-prefix
        assertEquals("otherns__MyObj__c", resolveType.invoke(api.sobject(), "otherns__MyObj__c"));
        assertEquals("otherns__MyEvent__e", resolveType.invoke(api.sobject(), "otherns__MyEvent__e"));

        SdkConfig noNsConfig = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(ACCESS_TOKEN)
                .setLoginEndpoint(DOMAIN)
                .setCustomObjectNamespace("");
        SforceApi noNsApi = new SforceApi(noNsConfig);
        assertEquals("MyObj__c", resolveType.invoke(noNsApi.sobject(), "MyObj__c"));
    }

    @Test
    void getCustomObjectTypeHandlesNamespacedAndPlainNames() throws Exception {
        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(ACCESS_TOKEN)
                .setLoginEndpoint(DOMAIN)
                .setCustomObjectNamespace("myns");
        SforceApi api = new SforceApi(config);

        Method method = BaseApi.class.getDeclaredMethod("getCustomObjectType", String.class);
        method.setAccessible(true);

        // Plain name — prepend global ns
        assertEquals("myns__MyObj__c", method.invoke(api.sobject(), "MyObj"));
        // Already namespaced — keep as-is
        assertEquals("otherns__MyObj__c", method.invoke(api.sobject(), "otherns__MyObj"));
        // With __c suffix, already namespaced — strip and re-append
        assertEquals("otherns__MyObj__c", method.invoke(api.sobject(), "otherns__MyObj__c"));
    }

    @Test
    void resolveTypeReturnsAsIsWhenAutoResolveDisabled() throws Exception {
        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken(ACCESS_TOKEN)
                .setLoginEndpoint(DOMAIN)
                .setCustomObjectNamespace("myns")
                .setAutoResolveCustomObjects(false);
        SforceApi api = new SforceApi(config);

        Method resolveType = BaseApi.class.getDeclaredMethod("resolveType", String.class);
        resolveType.setAccessible(true);

        assertEquals("MyObj__c", resolveType.invoke(api.sobject(), "MyObj__c"));
        assertEquals("MyEvent__e", resolveType.invoke(api.sobject(), "MyEvent__e"));
    }

    @Test
    void batchDeleteObjectsRejectsNullIds() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200, "[]"));
        assertThrows(IllegalArgumentException.class,
                () -> api.sobject().batchDeleteObjects(null, false));
    }

    @Test
    void batchDeleteObjectsRejectsEmptyIds() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200, "[]"));
        assertThrows(IllegalArgumentException.class,
                () -> api.sobject().batchDeleteObjects(Collections.emptyList(), false));
    }

    @Test
    void batchDeleteObjectsRejectsOversizedIds() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200, "[]"));
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 201; i++) ids.add("id" + i);
        assertThrows(IllegalArgumentException.class,
                () -> api.sobject().batchDeleteObjects(ids, false));
    }

    @Test
    void batchDeleteObjectsSendsCorrectUrl() {
        AtomicReference<Request> captured = new AtomicReference<>();
        SforceApi api = apiWith(chain -> {
            captured.set(chain.request());
            return buildResponse(chain.request(), 200, "[{\"id\":\"id1\",\"success\":true}]");
        });

        List<DeleteObjectResponse> responses = api.sobject().batchDeleteObjects(List.of("id1", "id2", "id3"), true);
        Request request = captured.get();
        assertNotNull(request);
        assertEquals("DELETE", request.method());
        String url = request.url().toString();
        assertTrue(url.contains("?ids=id1,id2,id3"));
        assertTrue(url.contains("allOrNone=true"));
        assertEquals(1, responses.size());
        assertEquals("id1", responses.get(0).id());
    }

    @Test
    void soqlQueryAllFetchesAllPages() {
        AtomicInteger counter = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            int page = counter.incrementAndGet();
            if (page == 1) {
                return buildResponse(chain.request(), 200,
                        "{\"totalSize\":2,\"done\":false,\"records\":[{\"Name\":\"A\"}],\"nextRecordsUrl\":\"/services/data/v62.0/query/01g\"}");
            }
            return buildResponse(chain.request(), 200,
                    "{\"totalSize\":1,\"done\":true,\"records\":[{\"Name\":\"B\"}]}");
        });

        PageQueryResponse<Map> response = api.query().soqlQueryAll("SELECT Name FROM Account", Map.class);
        assertEquals(2, response.getTotalSize());
        assertTrue(response.getDone());
        assertEquals(2, response.getRecords().size());
        assertEquals("A", response.getRecords().get(0).get("Name"));
        assertEquals("B", response.getRecords().get(1).get("Name"));
        assertEquals(2, counter.get());
    }

    @Test
    void batchCreateSObjectsPartitionsAt200() {
        AtomicInteger counter = new AtomicInteger();
        SforceApi api = apiWith(chain -> {
            counter.incrementAndGet();
            return buildResponse(chain.request(), 200,
                    "[{\"id\":\"001xx\",\"success\":true},{\"id\":\"001xx\",\"success\":true}]");
        });

        List<CompositeBodyObject> records = new ArrayList<>();
        for (int i = 0; i < 201; i++) {
            CompositeBodyObject obj = new CompositeBodyObject();
            obj.setBody(Map.of("Name", "Record" + i));
            records.add(obj);
        }

        List<CreateObjectResponse> responses = api.sobject().batchCreateSObjects(records);
        assertEquals(2, counter.get());
        assertFalse(responses.isEmpty());
    }

    @Test
    void batchCreateOrUpdateSObjectsHandlesPartialFailures() {
        SforceApi api = apiWith(chain -> buildResponse(chain.request(), 200,
                "{\"compositeResponse\":[" +
                        "{\"body\":{\"id\":\"001xx\",\"success\":true,\"created\":true},\"httpHeaders\":{},\"httpStatusCode\":200,\"referenceId\":\"k1\"}," +
                        "{\"body\":[{\"errorCode\":\"FIELD_CUSTOM_VALIDATION_EXCEPTION\",\"message\":\"bad\"}],\"httpHeaders\":{},\"httpStatusCode\":400,\"referenceId\":\"k2\"}" +
                        "]}"));

        Map<String, Object> body1 = Map.of("Name", "One");
        Map<String, Object> body2 = Map.of("Name", "Two");

        Map<String, CreateOrUpdateObjectResponse> result = api.sobject().batchCreateOrUpdateSObjects(
                "Account", "External_Id__c", Map.of("k1", body1, "k2", body2));

        assertEquals(2, result.size());
        assertTrue(result.get("k1").isSuccess());
        assertFalse(result.get("k2").isSuccess());
        assertNotNull(result.get("k2").getErrors());
    }
}
