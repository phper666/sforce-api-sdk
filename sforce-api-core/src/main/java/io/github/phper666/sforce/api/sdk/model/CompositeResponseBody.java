package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Data
public class CompositeResponseBody {
    private List<CompositeResponse> compositeResponse;
}
