package com.phper666.sforce.api.sdk.exception;

import java.io.Serial;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
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
