package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;

/**
 * Represents a single object entry from {@code GET /services/data/vXX.X/sobjects/}.
 * <p>
 * Only objects the current user has permission to access are returned.
 *
 * @see <a href="https://developer.salesforce.com/docs/atlas.en-us.252.0.api.meta/api/sforce_api_calls_describesobjects_describesobjectresult.htm">DescribeGlobalResult</a>
 */
@Data
public class SObjectMetadata {
    private String name;
    private String label;
    private boolean custom;
    private boolean queryable;
    private boolean createable;
    private boolean updateable;
    private boolean deletable;
    private String keyPrefix;
    private String labelPlural;
}
