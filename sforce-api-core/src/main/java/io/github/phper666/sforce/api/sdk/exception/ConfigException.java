package io.github.phper666.sforce.api.sdk.exception;

import java.io.Serial;

/**
 * Exception for SDK configuration errors.
 *
 * @author Yuzhao.Li
 */
public class ConfigException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String message;

    public ConfigException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ConfigException(message:%s)".formatted(message);
    }
}
