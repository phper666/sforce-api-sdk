package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Error detail returned by a Salesforce Composite API subrequest.
 *
 * @author Yuzhao.Li
 */
@Data
@Accessors(chain = true)
public class CompositeResponseError {
    private String message;
    private String errorCode;
}
