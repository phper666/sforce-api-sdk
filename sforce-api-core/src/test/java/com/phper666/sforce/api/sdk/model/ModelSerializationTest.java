package com.phper666.sforce.api.sdk.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.phper666.sforce.api.sdk.BulkApi;
import com.phper666.sforce.api.sdk.serialize.CustomParameterizedType;
import com.phper666.sforce.api.sdk.serialize.GsonJsonSerializer;
import com.phper666.sforce.api.sdk.serialize.JsonSerializer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class ModelSerializationTest {

    private final JsonSerializer serializer = GsonJsonSerializer.INSTANCE();

    @Test
    void createObjectResponseSerialization() {
        String json = "{\"id\":\"001xx000003DGbA\",\"success\":true,\"errors\":[]}";
        CreateObjectResponse response = (CreateObjectResponse) serializer.fromJson(json, CreateObjectResponse.class);
        assertEquals("001xx000003DGbA", response.getId());
        assertTrue(response.isSuccess());
    }

    @Test
    void createObjectResponseFailure() {
        String json = "{\"success\":false,\"errors\":[{\"message\":\"Required field missing\",\"statusCode\":\"REQUIRED_FIELD_MISSING\"}],\"id\":null}";
        CreateObjectResponse response = (CreateObjectResponse) serializer.fromJson(json, CreateObjectResponse.class);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrors());
    }

    @Test
    void createOrUpdateObjectResponse() {
        String json = "{\"id\":\"001xx000003DGbA\",\"success\":true,\"created\":true}";
        CreateOrUpdateObjectResponse response = (CreateOrUpdateObjectResponse) serializer.fromJson(json, CreateOrUpdateObjectResponse.class);
        assertEquals("001xx000003DGbA", response.getId());
        assertTrue(response.isSuccess());
        assertTrue(response.isCreated());
    }

    @Test
    void deleteObjectResponse() {
        String json = "{\"id\":\"001xx000003DGbA\",\"success\":true,\"errors\":[]}";
        DeleteObjectResponse response = (DeleteObjectResponse) serializer.fromJson(json, DeleteObjectResponse.class);
        assertEquals("001xx000003DGbA", response.id());
        assertTrue(response.success());
    }

    @Test
    void pageQueryResponse() {
        String json = "{\"totalSize\":1,\"done\":true,\"records\":[{\"Id\":\"001xx000003DGbA\",\"Name\":\"Test\"}],\"nextRecordsUrl\":null}";
        Type type = new CustomParameterizedType(PageQueryResponse.class, new Class[]{Map.class});
        PageQueryResponse<Map> response = (PageQueryResponse<Map>) serializer.fromJson(json, type);
        assertEquals(1, response.getTotalSize());
        assertTrue(response.getDone());
        assertNull(response.getNextRecordsUrl());
        assertEquals(1, response.getRecords().size());
        assertEquals("Test", response.getRecords().get(0).get("Name"));
    }

    @Test
    void pageQueryResponseWithCustomClass() {
        String json = "{\"totalSize\":1,\"done\":true,\"records\":[{\"name\":\"Test\"}]}";
        Type type = new CustomParameterizedType(PageQueryResponse.class, new Class[]{Account.class});
        PageQueryResponse<Account> response = (PageQueryResponse<Account>) serializer.fromJson(json, type);
        assertEquals(1, response.getRecords().size());
        assertEquals("Test", response.getRecords().get(0).name);
    }

    @Test
    void responseErrorDtoArray() {
        String json = "[{\"message\":\"insufficient access rights\",\"statusCode\":\"INSUFFICIENT_ACCESS\",\"fields\":[]}]";
        ResponseErrorDto[] errors = (ResponseErrorDto[]) serializer.fromJson(json, ResponseErrorDto[].class);
        assertEquals(1, errors.length);
        assertEquals("insufficient access rights", errors[0].getMessage());
        assertEquals("INSUFFICIENT_ACCESS", errors[0].getStatusCode());
    }

    @Test
    void responseErrorDtoList() {
        String json = "[{\"message\":\"insufficient access rights\",\"statusCode\":\"INSUFFICIENT_ACCESS\"}]";
        List<ResponseErrorDto> errors = serializer.fromJsonList(json, ResponseErrorDto.class);
        assertEquals(1, errors.size());
        assertEquals("insufficient access rights", errors.get(0).getMessage());
    }

    @Test
    void compositeResponse() {
        String json = "{\"body\":{\"Id\":\"001xx000003DGbA\",\"Name\":\"Test\"},\"httpHeaders\":{},\"httpStatusCode\":200,\"referenceId\":\"ref1\"}";
        CompositeResponse response = (CompositeResponse) serializer.fromJson(json, CompositeResponse.class);
        assertEquals(200, response.getHttpStatusCode());
        assertTrue(response.isSuccessful());
        assertEquals("ref1", response.getReferenceId());
    }

    @Test
    void compositeResponseBody() {
        String json = "{\"compositeResponse\":[{\"body\":{\"Id\":\"001xx\"},\"httpStatusCode\":201,\"referenceId\":\"ref1\"}]}";
        CompositeResponseBody response = (CompositeResponseBody) serializer.fromJson(json, CompositeResponseBody.class);
        assertNotNull(response.getCompositeResponse());
        assertEquals(1, response.getCompositeResponse().size());
        assertEquals(201, response.getCompositeResponse().get(0).getHttpStatusCode());
    }

    @Test
    void compositeResponseError() {
        String json = "[{\"message\":\"Duplicate value\",\"errorCode\":\"DUPLICATE_VALUE\"}]";
        List<CompositeResponseError> errors = serializer.fromJsonList(json, CompositeResponseError.class);
        assertEquals(1, errors.size());
        assertEquals("Duplicate value", errors.get(0).getMessage());
        assertEquals("DUPLICATE_VALUE", errors.get(0).getErrorCode());
    }

    @Test
    void updateObjectResponse() {
        String json = "{\"id\":\"001xx000003DGbA\",\"success\":true,\"errors\":[]}";
        UpdateObjectResponse response = (UpdateObjectResponse) serializer.fromJson(json, UpdateObjectResponse.class);
        assertEquals("001xx000003DGbA", response.id());
        assertTrue(response.success());
    }

    @Test
    void soslQueryResponse() {
        String json = "{\"searchRecords\":[{\"Id\":\"001xx\"}],\"metadata\":null}";
        SOSLQueryResponse response = (SOSLQueryResponse) serializer.fromJson(json, SOSLQueryResponse.class);
        assertNotNull(response.getSearchRecords());
        assertEquals(1, response.getSearchRecords().size());
    }

    @Test
    void parameterizedSearchRequestBodyRoundTrip() {
        ParameterizedSearchRequestBody body = new ParameterizedSearchRequestBody(
                "test", "10", null, new String[]{"Name"}, null, null, null, null,
                null, null, null, null, null, true, null, null);

        String json = serializer.toJson(body);
        ParameterizedSearchRequestBody parsed = (ParameterizedSearchRequestBody) serializer.fromJson(json, ParameterizedSearchRequestBody.class);
        assertEquals("test", parsed.q());
        assertEquals("10", parsed.defaultLimit());
        assertArrayEquals(new String[]{"Name"}, parsed.fields());
        assertTrue(parsed.spellCorrection());
    }

    @Test
    void compositeObjectSerialization() {
        CompositeObject obj = new CompositeObject();
        obj.setObjectType("Account").setAttributes("Name", "Test Account");
        String json = serializer.toJson(obj);
        assertTrue(json.contains("\"type\":\"Account\""));
        assertTrue(json.contains("\"Name\":\"Test Account\""));
    }

    @Test
    void compositeBodyObjectGetBody() {
        CompositeBodyObject obj = new CompositeBodyObject();
        obj.setObjectType("Account");
        obj.setBody(Map.of("Name", "Test"));

        Object body = obj.getBody();
        assertInstanceOf(JsonObject.class, body);
        JsonObject json = (JsonObject) body;
        assertEquals("Test", json.get("Name").getAsString());
        JsonElement attributes = json.get("attributes");
        assertNotNull(attributes);
        assertEquals("Account", attributes.getAsJsonObject().get("type").getAsString());
    }

    @Test
    void bulkApiCreateJobRequestSerialization() {
        BulkApiCreateJobRequest request = new BulkApiCreateJobRequest();
        request.setObject("Account");
        request.setOperation(BulkApi.JobOperation.INSERT);
        request.setLineEnding(BulkApi.LineEnding.LF);
        request.setColumnDelimiter(BulkApi.ColumnDelimiter.COMMA);
        request.setExternalIdFieldName("ExtId__c");

        String json = serializer.toJson(request);
        assertTrue(json.contains("\"object\":\"Account\""));
        assertTrue(json.contains("\"operation\":\"insert\""));
        assertTrue(json.contains("\"lineEnding\":\"LF\""));
        assertTrue(json.contains("\"columnDelimiter\":\"COMMA\""));
        assertTrue(json.contains("\"externalIdFieldName\":\"ExtId__c\""));
    }

    @Test
    void bulkApiJobDetailResponseDeserialization() {
        String json = "{\"id\":\"750xx000000000\",\"operation\":\"insert\",\"state\":\"UploadComplete\",\"lineEnding\":\"LF\",\"columnDelimiter\":\"COMMA\",\"contentUrl\":\"/services/data/v62.0/jobs/ingest/750/batches\"}";
        BulkApiJobDetailResponse response = (BulkApiJobDetailResponse) serializer.fromJson(json, BulkApiJobDetailResponse.class);
        assertEquals("750xx000000000", response.getId());
        assertEquals(BulkApi.JobOperation.INSERT, response.getOperation());
        assertEquals(BulkApi.JobState.UPDATE_COMPLETE, response.getState());
        assertEquals(BulkApi.LineEnding.LF, response.getLineEnding());
        assertEquals(BulkApi.ColumnDelimiter.COMMA, response.getColumnDelimiter());
        assertEquals("/services/data/v62.0/jobs/ingest/750/batches", response.getContentUrl());
    }

    @Test
    void objectDescribeResponse() {
        String json = "{\"name\":\"Account\",\"label\":\"Account\",\"queryable\":true,\"updateable\":true,\"fields\":[{\"name\":\"Id\",\"type\":\"id\",\"label\":\"Account ID\",\"updateable\":false}]}";
        ObjectDescribeResponse response = (ObjectDescribeResponse) serializer.fromJson(json, ObjectDescribeResponse.class);
        assertEquals("Account", response.getName());
        assertTrue(response.isQueryable());
        assertEquals(1, response.getFieldNames().size());
        assertEquals("Id", response.getFieldNames().get(0));
    }

    @Test
    void listInvocableActionResult() {
        String json = "{\"actions\":[{\"label\":\"My Action\",\"name\":\"MyAction\",\"type\":\"apex\",\"url\":\"/services/data/v62.0/actions/custom/apex/MyAction\"}]}";
        ListInvocableActionResult result = (ListInvocableActionResult) serializer.fromJson(json, ListInvocableActionResult.class);
        assertNotNull(result.getActions());
        assertEquals(1, result.getActions().size());
        assertEquals("My Action", result.getActions().get(0).label());
        assertEquals("apex", result.getActions().get(0).type());
    }

    @Test
    void downloadContentDocumentRequest() {
        DownloadContentDocumentRequest request = new DownloadContentDocumentRequest("069xx", "/tmp", "doc", ".pdf");

        String json = serializer.toJson(request);
        assertTrue(json.contains("\"fileId\":\"069xx\""));
        assertTrue(json.contains("\"directory\":\"/tmp\""));
        assertTrue(json.contains("\"prefix\":\"doc\""));
        assertTrue(json.contains("\"suffix\":\".pdf\""));
    }

    private static class Account {
        String name;
    }
}
