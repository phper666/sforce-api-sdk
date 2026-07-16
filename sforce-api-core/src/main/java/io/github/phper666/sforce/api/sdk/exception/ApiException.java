package io.github.phper666.sforce.api.sdk.exception;

import java.io.Serial;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public class ApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final int code;
    private final String message;
    private String method;
    private String url;

    public ApiException(int code, String message, String method, String url) {
        super(message);
        this.code = code;
        this.message = message;
        this.method = method;
        this.url = url;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
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
