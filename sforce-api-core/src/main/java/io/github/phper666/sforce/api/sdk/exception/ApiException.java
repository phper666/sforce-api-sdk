package io.github.phper666.sforce.api.sdk.exception;

import io.github.phper666.sforce.api.sdk.model.ApiErrorDetail;

import java.io.Serial;
import java.util.List;

/**
 * Exception for Salesforce API request failures.
 * <p>
 * {@link #getMessage()} keeps returning the raw response body for backward
 * compatibility. Use {@link #getErrorCode()} / {@link #getErrorDetails()} to
 * read the structured error when Salesforce returned a documented error body.
 *
 * @author Yuzhao.Li
 */
public class ApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final int code;
    private final String message;
    private final List<ApiErrorDetail> errorDetails;
    private String method;
    private String url;

    public ApiException(int code, String message, String method, String url) {
        this(code, message, method, url, List.of());
    }

    public ApiException(int code, String message, String method, String url, List<ApiErrorDetail> errorDetails) {
        super(message);
        this.code = code;
        this.message = message;
        this.method = method;
        this.url = url;
        this.errorDetails = errorDetails == null ? List.of() : List.copyOf(errorDetails);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Raw Salesforce response body, exactly as received.
     *
     * @return the unparsed response body, never null
     */
    public String getRawBody() {
        return message;
    }

    /**
     * Structured error entries parsed from the response body.
     *
     * @return parsed error details, empty when the body isn't a documented error shape
     */
    public List<ApiErrorDetail> getErrorDetails() {
        return errorDetails;
    }

    /**
     * First Salesforce error code parsed from the response body.
     *
     * @return the error code, or null when the body isn't a documented error shape
     */
    public String getErrorCode() {
        return errorDetails.isEmpty() ? null : errorDetails.get(0).errorCode();
    }

    @Override
    public String toString() {
        return "ApiException(code:%s, message:%s, method:%s, url:%s)".formatted(code, message, method, url);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
