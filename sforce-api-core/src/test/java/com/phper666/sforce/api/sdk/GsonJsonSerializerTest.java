package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.serialize.CustomParameterizedType;
import com.phper666.sforce.api.sdk.serialize.GsonJsonSerializer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class GsonJsonSerializerTest {

    private final GsonJsonSerializer serializer = GsonJsonSerializer.INSTANCE();

    @Test
    void instanceReturnsSingleton() {
        assertSame(GsonJsonSerializer.INSTANCE(), GsonJsonSerializer.INSTANCE());
    }

    @Test
    void toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "test");
        data.put("count", 42);
        String json = serializer.toJson(data);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"test\""));
        assertTrue(json.contains("\"count\""));
        assertTrue(json.contains("42"));
    }

    @Test
    void toJsonNullReturnsNullLiteral() {
        assertEquals("null", serializer.toJson(null));
    }

    @Test
    void fromJsonWithClass() {
        String json = "{\"name\":\"test\",\"count\":42}";
        Map result = (Map) serializer.fromJson(json, Map.class);
        assertEquals("test", result.get("name"));
        assertEquals(42.0, result.get("count"));
    }

    @Test
    void fromJsonWithType() {
        String json = "[\"a\",\"b\",\"c\"]";
        Type listType = new CustomParameterizedType(List.class, new Type[]{String.class});
        List<String> result = (List<String>) serializer.fromJson(json, listType);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    void fromJsonList() {
        String json = "[{\"name\":\"a\"},{\"name\":\"b\"}]";
        List<Map> result = serializer.fromJsonList(json, Map.class);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0).get("name"));
        assertEquals("b", result.get(1).get("name"));
    }

    @Test
    void fromJsonListOfStrings() {
        String json = "[\"a\",\"b\"]";
        List<String> result = serializer.fromJsonList(json, String.class);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
    }
}
