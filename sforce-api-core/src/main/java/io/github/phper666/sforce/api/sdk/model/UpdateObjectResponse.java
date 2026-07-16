package io.github.phper666.sforce.api.sdk.model;

import java.util.List;

/**
 * Response from an update SObject operation.
 *
 * @author Yuzhao.Li
 */
public record UpdateObjectResponse(String id, boolean success, List<ResponseErrorDto> errors) {}
