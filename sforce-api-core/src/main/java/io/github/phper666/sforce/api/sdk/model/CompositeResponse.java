package io.github.phper666.sforce.api.sdk.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Response for a single subrequest in a Salesforce Composite API call.
 *
 * @author Yuzhao.Li
 */
@Getter
@Setter
public class CompositeResponse {
    private Object body;
    private Map<String, String> httpHeaders;
    private Integer httpStatusCode;
    private String referenceId;
    private List<CompositeResponseError> compositeResponseErrors = new ArrayList<>();
    private Boolean duplicateValueError = false;

    public boolean isSuccessful() {
        return httpStatusCode >= 200 && httpStatusCode < 300;
    }
}
