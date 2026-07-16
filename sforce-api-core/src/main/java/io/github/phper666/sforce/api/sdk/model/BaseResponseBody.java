package io.github.phper666.sforce.api.sdk.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Base response body for Salesforce API responses.
 *
 * @author Yuzhao.Li
 */
@Getter
@Setter
public class BaseResponseBody {
    private Object errors;
}
