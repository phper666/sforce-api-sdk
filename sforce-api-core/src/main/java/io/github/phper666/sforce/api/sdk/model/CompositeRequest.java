package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;

/**
 * Represents a subrequest in a Salesforce Composite API call.
 *
 * @author Yuzhao.Li
 */
@Data
public class CompositeRequest {
    private String method;
    private String url;
    private Object body;
    private Object referenceId;
}
