package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * Response body wrapper for a Salesforce Composite API call.
 *
 * @author Yuzhao.Li
 */
@Data
public class CompositeResponseBody {
    private List<CompositeResponse> compositeResponse;
}
