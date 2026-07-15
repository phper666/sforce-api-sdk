package com.phper666.sforce.api.sdk.model;

import lombok.Data;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Data
public class CompositeRequest {
    private String method;
    private String url;
    private Object body;
    private Object referenceId;
}
