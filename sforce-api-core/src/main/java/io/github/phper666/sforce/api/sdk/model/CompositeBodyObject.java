package io.github.phper666.sforce.api.sdk.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.phper666.sforce.api.sdk.serialize.GsonJsonSerializer;

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
public class CompositeBodyObject extends CompositeObject {
    private String body;

    public CompositeObject setBody(Object body) {
        var gson = GsonJsonSerializer.INSTANCE();
        this.body = gson.toJson(body);
        return this;
    }

    public Object getBody() {
        var gson = GsonJsonSerializer.INSTANCE();
        var jsonObject = (JsonObject) gson.fromJson(this.body, JsonObject.class);
        if (!this.getAttributes().isEmpty()) {
            var jsonElement = (JsonElement) gson.fromJson(gson.toJson(this.getAttributes()), JsonElement.class);
            jsonObject.add("attributes", jsonElement);
        }
        return jsonObject;
    }
}
