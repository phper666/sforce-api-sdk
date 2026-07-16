package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * Request body for a Salesforce Composite API call.
 *
 * @author Yuzhao.Li
 */
@Data
public class CompositeRequestBody {
    private boolean allOrNone;
    private boolean collateSubrequests;
    private List<CompositeRequest> compositeRequest;
}
