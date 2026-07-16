package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @see <a href="https://developer.salesforce.com/docs/atlas.en-us.252.0.api.meta/api/sforce_api_calls_describesobjects_describesobjectresult.htm">DescribeSObjectResult</a>
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Data
public class ObjectDescribeResponse {
    private String name;
    private String label;
    private boolean queryable;
    private boolean updateable;
    private List<ObjectFieldDescribe> fields;

    public List<String> getFieldNames() {
        return Optional.ofNullable(fields).map(list -> list.stream().map(ObjectFieldDescribe::getName).toList()).orElse(Collections.emptyList());
    }
}
