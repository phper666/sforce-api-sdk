package io.github.phper666.sforce.api.sdk.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
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
