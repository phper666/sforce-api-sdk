package io.github.phper666.sforce.api.sdk.internal;

import io.github.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * OkHttp interceptor for per-request timeout handling.
 *
 * @author Yuzhao.Li
 */
public class TimeoutInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        TimeoutSettings timeOutConfig = request.tag(TimeoutSettings.class);
        if (Objects.nonNull(timeOutConfig) && Objects.nonNull(timeOutConfig.getTimeOut())) {
            chain = chain.withReadTimeout(timeOutConfig.getTimeOut(), timeOutConfig.getTimeUnit());
            chain = chain.withWriteTimeout(timeOutConfig.getTimeOut(), timeOutConfig.getTimeUnit());
        }
        return chain.proceed(request);
    }
}
