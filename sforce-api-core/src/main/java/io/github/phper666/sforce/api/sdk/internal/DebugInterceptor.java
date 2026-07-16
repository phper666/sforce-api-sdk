package io.github.phper666.sforce.api.sdk.internal;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Slf4j
public class DebugInterceptor implements Interceptor {
    private final boolean logBody;
    private final int bodyMaxSize;

    public DebugInterceptor(boolean logBody, int bodyMaxSize) {
        this.logBody = logBody;
        this.bodyMaxSize = bodyMaxSize;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        var request = chain.request();
        var t1 = System.nanoTime();

        // Log request: method, URL, headers (WITHOUT Authorization value)
        if (log.isDebugEnabled()) {
            var auth = request.header("Authorization");
            var safeAuth = auth != null ? "Bearer ***" : "none";
            log.debug("→ {} {} [Authorization: {}]", request.method(), request.url(), safeAuth);
        }

        var response = chain.proceed(request);
        var t2 = System.nanoTime();
        var ms = (t2 - t1) / 1_000_000;

        if (log.isDebugEnabled()) {
            log.debug("← {} {} {} ({}ms)", response.code(), response.request().url(), response.message(), ms);
        }

        if (logBody && log.isTraceEnabled()) {
            // Log request body
            if (request.body() != null) {
                try {
                    var buffer = new Buffer();
                    request.body().writeTo(buffer);
                    var reqBody = buffer.readUtf8();
                    log.trace("Request body: {}", truncate(reqBody));
                } catch (Exception e) {
                    log.trace("Request body: <unreadable>");
                }
            }
            // Log response body
            var responseBody = response.peekBody(bodyMaxSize);
            var resBody = responseBody.string();
            log.trace("Response body: {}", truncate(resBody));
        }

        return response;
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > bodyMaxSize
            ? "%s... (%d bytes)".formatted(s.substring(0, bodyMaxSize), s.length())
            : s;
    }
}
