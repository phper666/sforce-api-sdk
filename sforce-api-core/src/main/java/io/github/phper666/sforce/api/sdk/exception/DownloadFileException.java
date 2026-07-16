package io.github.phper666.sforce.api.sdk.exception;

import java.io.Serial;

/**
 * Exception for file download failures.
 *
 * @author Yuzhao.Li
 */
public class DownloadFileException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;
    private final String message;

    public DownloadFileException(String message) {
        this.code = 0;
        this.message = message;
    }

    public DownloadFileException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
