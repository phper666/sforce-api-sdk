package com.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Data
public class ResponseErrorDto {
    private String statusCode;
    private String message;
    private List<String> fields;
}