package io.github.phper666.sforce.api.sdk.model;

import java.util.List;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public record DeleteObjectResponse(String id, boolean success, List<ResponseErrorDto> errors) {}
