package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * Error detail returned by a Salesforce API operation.
 *
 * @author Yuzhao.Li
 */
@Data
public class ResponseErrorDto {
    private String statusCode;
    private String message;
    private List<String> fields;
}