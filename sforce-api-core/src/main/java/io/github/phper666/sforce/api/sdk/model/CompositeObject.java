package io.github.phper666.sforce.api.sdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * body:
 * {
 *       "attributes" : {"type" : "Account"},
 *       "Name" : "example.com",
 *       "BillingCity" : "San Francisco"
 *    }
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public class CompositeObject {
    private final static String TYPE = "type";

    private Map<String, Object> attributes = new HashMap<>();

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public CompositeObject setAttributes(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    public CompositeObject setObjectType(String objectType) {
        return setAttributes(TYPE, objectType);
    }
}
