package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * Response wrapper for a Salesforce SOSL search.
 *
 * @author Yuzhao.Li
 */
@Data
public class SOSLQueryResponse {
    private List<Object> searchRecords;
    private Object metadata;
}

