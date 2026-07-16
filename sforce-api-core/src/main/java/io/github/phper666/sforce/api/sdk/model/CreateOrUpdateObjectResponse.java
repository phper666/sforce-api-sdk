package io.github.phper666.sforce.api.sdk.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Response from a create-or-update SObject operation.
 *
 * @author Yuzhao.Li
 */
@Getter
@Setter
public class CreateOrUpdateObjectResponse extends CreateObjectResponse {
    private boolean created;
}
