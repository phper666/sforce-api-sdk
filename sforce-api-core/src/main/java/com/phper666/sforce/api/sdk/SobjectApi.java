package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.Session;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;
import com.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import com.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import com.phper666.sforce.api.sdk.internal.BaseApi;
import com.phper666.sforce.api.sdk.model.*;
import com.phper666.sforce.api.sdk.serialize.CustomParameterizedType;
import com.phper666.sforce.api.sdk.serialize.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
class SobjectApi extends BaseApi {
    private static final String COMMA = ",";
    private static final TimeoutSettings DEFAULT_TIME_OUT = new TimeoutSettings();

    SobjectApi(SdkConfig config, Session session, OkHttpClient okHttpClient, JsonSerializer jsonSerializer, BaseAuthenticator authFlow) {
        super(config, session, okHttpClient, jsonSerializer, authFlow);
    }
    private static final Map<String, String> EMPTY_HEADERS = null;
    private static final int MAX_BATCH_GET_SIZE = 2000;
    private static final int MAX_BATCH_UPSERT_SIZE = 200;
    private static final int MAX_BATCH_DELETE_SIZE = 200;
    private static final int COMPOSITE_REQUEST_LIMIT = 25;
    // SObject CRUD
    // ──────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public CreateObjectResponse createSObject(String objectType, Object sObject) {
        return createSObject(objectType, sObject, EMPTY_HEADERS);
    }
    @SuppressWarnings("unchecked")
    public CreateObjectResponse createSObject(String objectType, Object sObject, Map<String, String> headers) {
        String url = uriBase(objectType);
        RequestBody rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(sObject));
        try {
            String body = executeGetBody(url, HttpMethod.POST.name(), rb, headers, DEFAULT_TIME_OUT);
            CreateObjectResponse resp = (CreateObjectResponse) jsonSerializer.fromJson(body, CreateObjectResponse.class);
            log.info("createSObject finish, is_success:{}, object_type:{}, object_id:{}", resp.isSuccess(), objectType, resp.getId());
            return resp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteSObject(String objectType, String id) {
        try {
            executeGetBody(uriBase(objectType) + "/" + id, HttpMethod.DELETE.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T getSObject(String objectType, String id, Class<T> tClass) {
        try {
            String body = executeGetBody(uriBase(objectType) + "/" + id, HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            return (T) jsonSerializer.fromJson(body, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSObjectAsMap(String objectType, String id) {
        try {
            String body = executeGetBody(uriBase(objectType) + "/" + id, HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            return (Map<String, Object>) jsonSerializer.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T getSObjectByExternalId(String objectType, String externalIdField, String externalId, Class<T> tClass) {
        try {
            String url = uriBase(objectType) + "/" + externalIdField + "/" + URLEncoder.encode(externalId, StandardCharsets.UTF_8);
            String body = executeGetBody(url, HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            return (T) jsonSerializer.fromJson(body, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getCObjectByExternalId(String objectType, String externalIdField, String externalId, Class<T> tClass) {
        return getSObjectByExternalId(getCustomObjectType(objectType), externalIdField, externalId, tClass);
    }
    @SuppressWarnings("unchecked")
    public <T> List<T> batchGetSObjects(String objectType, List<String> ids, List<String> fields, Class<T> tClass) {
        var result = new ArrayList<T>();
        var type = new CustomParameterizedType(List.class, new Type[]{tClass});
        for (var partition : Lists.partition(ids, MAX_BATCH_GET_SIZE)) {
            var request = new HashMap<String, Object>();
            request.put("ids", partition);
            request.put("fields", fields);
            var rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(request));
            try {
                String body = executeGetBody(buildCompositeObjectUri() + "/" + objectType, HttpMethod.POST.name(), rb, EMPTY_HEADERS, DEFAULT_TIME_OUT);
                result.addAll((List<T>) jsonSerializer.fromJson(body, type));
            } catch (IOException e) {
                throw requestFailed("batchGetSObjects", e);
            }
        }
        return result;
    }
    public void updateSObject(String objectType, String id, Object sObject) {
        updateSObject(objectType, id, sObject, EMPTY_HEADERS);
    }
    public void updateSObject(String objectType, String id, Object sObject, Map<String, String> headers) {
        String url = uriBase(objectType) + "/" + id + "?_HttpMethod=PATCH";
        RequestBody rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(sObject));
        try {
            executeGetBody(url, HttpMethod.POST.name(), rb, headers, DEFAULT_TIME_OUT);
            log.info("update object finish, object_type:{}, object_id:{}", objectType, id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public CreateOrUpdateObjectResponse createOrUpdateSObject(String objectType, String externalIdField, String externalIdValue, Object sObject) {
        return createOrUpdateSObject(objectType, externalIdField, externalIdValue, sObject, EMPTY_HEADERS);
    }
    @SuppressWarnings("unchecked")
    public CreateOrUpdateObjectResponse createOrUpdateSObject(String objectType, String externalIdField, String externalIdValue, Object sObject, Map<String, String> headers) {
        try {
            String url = uriBase(objectType) + "/" + externalIdField + "/" + URLEncoder.encode(externalIdValue, StandardCharsets.UTF_8) + "?_HttpMethod=PATCH";
            RequestBody rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(sObject));
            String body = executeGetBody(url, HttpMethod.POST.name(), rb, headers, DEFAULT_TIME_OUT);
            CreateOrUpdateObjectResponse resp = (CreateOrUpdateObjectResponse) jsonSerializer.fromJson(body, CreateOrUpdateObjectResponse.class);
            log.info("createOrUpdateSObject finish, is_success:{}, object_type:{}, external_id_field:{}, external_id:{}",
                    resp.isSuccess(), objectType, externalIdField, externalIdValue);
            return resp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ──────────────────────────────────────────────
    // Custom Object (CObject) helpers
    // ──────────────────────────────────────────────
    public String createCObject(String objectType, Object cObject) {
        return createSObject(getCustomObjectType(objectType), cObject).getId();
    }
    public String createPlatformEvent(String eventType, Object platformEvent) {
        return createSObject(getPlatformEventType(eventType), platformEvent).getId();
    }
    public void updateCObject(String objectType, String id, Object cObject) {
        updateSObject(getCustomObjectType(objectType), id, cObject, EMPTY_HEADERS);
    }
    public void deleteCObject(String objectType, String id) {
        deleteSObject(getCustomObjectType(objectType), id);
        log.info("deleteCObject finish, object_type:{}, id:{}", objectType, id);
    }
    public <T> T getCObject(String objectType, String id, Class<T> tClass) {
        return getSObject(getCustomObjectType(objectType), id, tClass);
    }
    public CreateOrUpdateObjectResponse createOrUpdateCObject(String objectType, String externalIdField, String externalIdValue, Object cObject) {
        return createOrUpdateSObject(getCustomObjectType(objectType), externalIdField, externalIdValue, cObject);
    }

    // ──────────────────────────────────────────────
    // Batch operations
    // ──────────────────────────────────────────────

    public List<CreateOrUpdateObjectResponse> batchCreateOrUpdateSObjects(String objectType, String externalIdField, List<? extends CompositeObject> compositeObjects) {
        return batchCreateOrUpdateSObjects(objectType, externalIdField, compositeObjects, EMPTY_HEADERS);
    }

    @SuppressWarnings("unchecked")
    public List<CreateOrUpdateObjectResponse> batchCreateOrUpdateSObjects(String objectType, String externalIdField, List<? extends CompositeObject> compositeObjects, Map<String, String> headers) {
        List<CreateOrUpdateObjectResponse> responses = new ArrayList<>();
        compositeObjects.forEach(co -> co.setObjectType(objectType));
        String url = buildCompositeObjectUri() + "/" + objectType + "/" + externalIdField + "?_HttpMethod=PATCH";
        CustomParameterizedType type = new CustomParameterizedType(List.class, new Type[]{CreateOrUpdateObjectResponse.class});
        Lists.partition(compositeObjects, MAX_BATCH_UPSERT_SIZE).forEach(partition -> {
            Map<String, Object> request = new HashMap<>();
            request.put("allOrNone", false);
            request.put("records", partition);
            RequestBody rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(request));
            try {
                String body = executeGetBody(url, HttpMethod.POST.name(), rb, headers, DEFAULT_TIME_OUT);
                List<CreateOrUpdateObjectResponse> items = (List<CreateOrUpdateObjectResponse>) jsonSerializer.fromJson(body, type);
                items.forEach(r -> log.info("batchCreateOrUpdateSObjects finish, object_type:{}, object_id:{}", objectType, r.getId()));
                responses.addAll(items);
            } catch (IOException e) {
                throw requestFailed("batchCreateOrUpdateSObjects", e);
            }
        });
        return responses;
    }

    public List<CreateObjectResponse> batchCreateSObjects(List<? extends CompositeBodyObject> compositeBodyObjects) {
        return batchCreateSObjects(compositeBodyObjects, EMPTY_HEADERS);
    }

    @SuppressWarnings("unchecked")
    public List<CreateObjectResponse> batchCreateSObjects(List<? extends CompositeBodyObject> compositeBodyObjects, Map<String, String> headers) {
        List<CreateObjectResponse> responses = new ArrayList<>();
        CustomParameterizedType type = new CustomParameterizedType(List.class, new Type[]{CreateObjectResponse.class});
        Lists.partition(compositeBodyObjects, MAX_BATCH_UPSERT_SIZE).forEach(partition -> {
            List<Object> bodies = partition.stream().map(CompositeBodyObject::getBody).toList();
            Map<String, Object> request = new HashMap<>();
            request.put("allOrNone", false);
            request.put("records", bodies);
            RequestBody rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(request));
            try {
                String body = executeGetBody(buildCompositeObjectUri(), HttpMethod.POST.name(), rb, headers, DEFAULT_TIME_OUT);
                List<CreateObjectResponse> items = (List<CreateObjectResponse>) jsonSerializer.fromJson(body, type);
                items.forEach(r -> log.info("batchCreateSObjects finish, is_success:{}, object_id:{}", r.isSuccess(), r.getId()));
                responses.addAll(items);
            } catch (IOException e) {
                throw requestFailed("batchCreateSObjects", e);
            }
        });
        return responses;
    }

    public List<UpdateObjectResponse> batchUpdateCObjects(List<? extends CompositeBodyObject> compositeBodyObjects) {
        return batchUpdateCObjects(compositeBodyObjects, EMPTY_HEADERS);
    }

    @SuppressWarnings("unchecked")
    public List<UpdateObjectResponse> batchUpdateCObjects(List<? extends CompositeBodyObject> compositeBodyObjects, Map<String, String> headers) {
        var responses = new ArrayList<UpdateObjectResponse>();
        var type = new CustomParameterizedType(List.class, new Type[]{UpdateObjectResponse.class});
        Lists.partition(compositeBodyObjects, MAX_BATCH_UPSERT_SIZE).forEach(partition -> {
            var bodies = partition.stream().map(CompositeBodyObject::getBody).toList();
            var request = new HashMap<String, Object>();
            request.put("allOrNone", false);
            request.put("records", bodies);
            var rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(request));
            try {
                String body = executeGetBody(buildCompositeObjectUri(), HttpMethod.PATCH.name(), rb, headers, DEFAULT_TIME_OUT);
                var items = (List<UpdateObjectResponse>) jsonSerializer.fromJson(body, type);
                items.forEach(r -> log.info("batchUpdateCObjects finish, is_success:{}, object_id:{}", r.success(), r.id()));
                responses.addAll(items);
            } catch (IOException e) {
                throw requestFailed("batchUpdateCObjects", e);
            }
        });
        return responses;
    }
    public List<CreateOrUpdateObjectResponse> batchCreateOrUpdateCObjects(String objectType, String externalIdField, List<? extends CompositeObject> compositeObjects) {
        return batchCreateOrUpdateSObjects(getCustomObjectType(objectType), externalIdField, compositeObjects);
    }

    @SuppressWarnings("unchecked")
    public Map<String, CreateOrUpdateObjectResponse> batchCreateOrUpdateSObjects(String objectType, String externalIdField, Map<String, ?> externalValueSObjectMaps) {
        return batchCreateOrUpdateSObjects(objectType, externalIdField, externalValueSObjectMaps, EMPTY_HEADERS);
    }

    @SuppressWarnings("unchecked")
    public Map<String, CreateOrUpdateObjectResponse> batchCreateOrUpdateSObjects(String objectType, String externalIdField, Map<String, ?> externalValueSObjectMaps, Map<String, String> headers) {
        Map<String, CreateOrUpdateObjectResponse> result = new HashMap<>();
        List<CompositeRequest> allRequests = new ArrayList<>();
        for (Map.Entry<String, ?> entry : externalValueSObjectMaps.entrySet()) {
            CompositeRequest r = new CompositeRequest();
            r.setUrl("/services/data/" + config.getApiVersion() + "/sobjects/" + objectType + "/" + externalIdField + "/" + URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            r.setBody(entry.getValue());
            r.setMethod(HttpMethod.PATCH.name());
            r.setReferenceId(entry.getKey());
            allRequests.add(r);
        }
        Lists.partition(allRequests, COMPOSITE_REQUEST_LIMIT).forEach(batch -> {
            CompositeRequestBody body = new CompositeRequestBody();
            body.setCompositeRequest(batch);
            RequestBody rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(body));
            try {
                String response = executeGetBody(buildCompositeUri(), HttpMethod.POST.name(), rb, headers, DEFAULT_TIME_OUT);
                CompositeResponseBody respBody = (CompositeResponseBody) jsonSerializer.fromJson(response, CompositeResponseBody.class);
                respBody.getCompositeResponse().forEach(cr -> {
                    String bodyStr = jsonSerializer.toJson(cr.getBody());
                    if (isSuccess(cr.getHttpStatusCode())) {
                        result.put(cr.getReferenceId(), (CreateOrUpdateObjectResponse) jsonSerializer.fromJson(bodyStr, CreateOrUpdateObjectResponse.class));
                    } else {
                        CreateOrUpdateObjectResponse err = new CreateOrUpdateObjectResponse();
                        err.setSuccess(false);
                        err.setErrors(bodyStr);
                        result.put(cr.getReferenceId(), err);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }
    public Map<String, CreateOrUpdateObjectResponse> batchCreateOrUpdateCObjects(String objectType, String externalIdField, Map<String, ?> externalValueCObjectMaps) {
        return batchCreateOrUpdateSObjects(getCustomObjectType(objectType), externalIdField, externalValueCObjectMaps);
    }

    // ──────────────────────────────────────────────
    // Batch delete
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<DeleteObjectResponse> batchDeleteObjects(List<String> ids, boolean allOrNone) {
        if (ids == null || ids.isEmpty() || ids.size() > MAX_BATCH_DELETE_SIZE) {
            throw new IllegalArgumentException("ids size should be between 1 and " + MAX_BATCH_DELETE_SIZE);
        }
        String url = buildCompositeObjectUri() + "?ids=" + String.join(COMMA, ids) + "&allOrNone=" + allOrNone;
        try {
            String body = executeGetBody(url, HttpMethod.DELETE.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            Type type = new TypeToken<List<DeleteObjectResponse>>() {}.getType();
            return (List<DeleteObjectResponse>) jsonSerializer.fromJson(body, type);
        } catch (IOException e) {
            throw requestFailed("batchDeleteObjects", e);
        }
    }
    // SObject Describe
    // ──────────────────────────────────────────────

    public String describeObject(String objectName) {
        try {
            return executeGetBody(uriBase(objectName) + "/describe", HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("unchecked")
    public ObjectDescribeResponse getSObjectDescribe(String objectType) {
        return (ObjectDescribeResponse) jsonSerializer.fromJson(describeObject(objectType), ObjectDescribeResponse.class);
    }
    @SuppressWarnings("unchecked")
    public ObjectDescribeResponse getCObjectDescribe(String objectType) {
        return (ObjectDescribeResponse) jsonSerializer.fromJson(describeObject(getCustomObjectType(objectType)), ObjectDescribeResponse.class);
    }

    // ──────────────────────────────────────────────
    // Platform Event
    // ──────────────────────────────────────────────

    public String getPlatformEventSchema(String platformEventApiName) {
        try {
            return executeGetBody(uriBase(platformEventApiName + "/eventSchema"), HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // Relationship queries
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public <T> PageQueryResponse<T> getSObjectsByRelationship(String objectType, String id, String relationshipName, Class<T> tClass) {
        try {
            String body = executeGetBody(uriBase(objectType) + "/" + id + "/" + relationshipName, HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            Type type = new CustomParameterizedType(PageQueryResponse.class, new Class[]{tClass});
            return (PageQueryResponse<T>) jsonSerializer.fromJson(body, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSObjectByRelationship(String objectType, String id, String relationshipName, Object body) {
        String url = uriBase(objectType) + "/" + id + "/" + relationshipName + "?_HttpMethod=PATCH";
        RequestBody rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(body));
        try {
            executeGetBody(url, HttpMethod.POST.name(), rb, EMPTY_HEADERS, DEFAULT_TIME_OUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ──────────────────────────────────────────────
    // Unified facade aliases
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public CreateObjectResponse create(String objectType, Object sObject) {
        return createSObject(resolveType(objectType), sObject);
    }

    @SuppressWarnings("unchecked")
    public CreateObjectResponse create(String objectType, Object sObject, Map<String, String> headers) {
        return createSObject(resolveType(objectType), sObject, headers);
    }

    public void delete(String objectType, String id) {
        deleteSObject(resolveType(objectType), id);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String objectType, String id, Class<T> tClass) {
        return getSObject(resolveType(objectType), id, tClass);
    }

    public void update(String objectType, String id, Object sObject) {
        updateSObject(resolveType(objectType), id, sObject);
    }

    public void update(String objectType, String id, Object sObject, Map<String, String> headers) {
        updateSObject(resolveType(objectType), id, sObject, headers);
    }

    public CreateOrUpdateObjectResponse upsert(String objectType, String externalIdField, String externalIdValue, Object sObject) {
        return createOrUpdateSObject(resolveType(objectType), externalIdField, externalIdValue, sObject);
    }

    @SuppressWarnings("unchecked")
    public CreateOrUpdateObjectResponse upsert(String objectType, String externalIdField, String externalIdValue, Object sObject, Map<String, String> headers) {
        return createOrUpdateSObject(resolveType(objectType), externalIdField, externalIdValue, sObject, headers);
    }

    @SuppressWarnings("unchecked")
    public <T> T getByExternalId(String objectType, String externalIdField, String externalId, Class<T> tClass) {
        return getSObjectByExternalId(resolveType(objectType), externalIdField, externalId, tClass);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> batchGet(String objectType, List<String> ids, List<String> fields, Class<T> tClass) {
        return batchGetSObjects(resolveType(objectType), ids, fields, tClass);
    }

    @SuppressWarnings("unchecked")
    public ObjectDescribeResponse describe(String objectType) {
        return getSObjectDescribe(resolveType(objectType));
    }

}
