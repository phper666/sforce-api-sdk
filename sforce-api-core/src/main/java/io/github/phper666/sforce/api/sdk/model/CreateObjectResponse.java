package io.github.phper666.sforce.api.sdk.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Response from a create SObject operation.
 *
 * @author Yuzhao.Li
 */
@Getter
@Setter
public class CreateObjectResponse extends BaseResponseBody {
    private String id;
    private boolean success;
}
