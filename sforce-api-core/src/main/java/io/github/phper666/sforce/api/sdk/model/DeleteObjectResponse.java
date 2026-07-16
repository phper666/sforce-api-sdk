package io.github.phper666.sforce.api.sdk.model;

import java.util.List;

/**
 * Response from a delete SObject operation.
 *
 * @author Yuzhao.Li
 */
public record DeleteObjectResponse(String id, boolean success, List<ResponseErrorDto> errors) {}
