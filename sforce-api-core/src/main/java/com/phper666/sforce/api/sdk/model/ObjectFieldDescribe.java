package com.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * @see <a href="https://developer.salesforce.com/docs/atlas.en-us.252.0.api.meta/api/sforce_api_calls_describesobjects_describesobjectresult.htm">Field</a>
 */
@Data
public class ObjectFieldDescribe {
    private String name;
    private String type;
    private String label;
    private boolean updateable;
    private boolean createable;
    private boolean nillable;
    private boolean filterable;
    private boolean unique;
    private boolean idLookup;
    private boolean externalId;
    private int length;
    private List<String> referenceTo;
    private String relationshipName;
    private String defaultValue;
    private String inlineHelpText;
    private List<PicklistEntry> picklistValues;

    @Data
    public static class PicklistEntry {
        private String value;
        private String label;
        private boolean active;
        private boolean defaultValue;
    }
}
