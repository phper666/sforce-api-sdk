package io.github.phper666.sforce.api.sdk.model;

import java.util.List;

/**
 * One error entry from a Salesforce REST API error response.
 * <p>
 * Mirrors the documented error response body, for example:
 * {@code [{"fields":["Id"],"message":"...","errorCode":"MALFORMED_ID"}]}.
 *
 * @param errorCode Salesforce error code, for example {@code MALFORMED_ID}
 * @param message   human readable error message
 * @param fields    fields related to the error, empty when not applicable
 * @author Yuzhao.Li
 */
public record ApiErrorDetail(String errorCode, String message, List<String> fields) {
}
