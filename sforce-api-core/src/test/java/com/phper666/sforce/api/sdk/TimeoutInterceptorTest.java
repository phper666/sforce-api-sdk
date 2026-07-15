package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import com.phper666.sforce.api.sdk.internal.TimeoutInterceptor;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class TimeoutInterceptorTest {

    @Test
    void appliesTimeoutWhenConfigPresent() throws Exception {
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        TimeoutSettings settings = new TimeoutSettings();
        settings.setTimeOut(30);
        settings.setTimeUnit(TimeUnit.SECONDS);
        Request request = new Request.Builder()
                .url("https://example.com")
                .tag(TimeoutSettings.class, settings)
                .build();

        when(chain.request()).thenReturn(request);
        when(chain.withReadTimeout(30, TimeUnit.SECONDS)).thenReturn(chain);
        when(chain.withWriteTimeout(30, TimeUnit.SECONDS)).thenReturn(chain);

        TimeoutInterceptor interceptor = new TimeoutInterceptor();
        interceptor.intercept(chain);

        verify(chain).withReadTimeout(30, TimeUnit.SECONDS);
        verify(chain).withWriteTimeout(30, TimeUnit.SECONDS);
        verify(chain).proceed(request);
    }

    @Test
    void skipsTimeoutWhenConfigNull() throws Exception {
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Request request = new Request.Builder()
                .url("https://example.com")
                .build();

        when(chain.request()).thenReturn(request);

        TimeoutInterceptor interceptor = new TimeoutInterceptor();
        interceptor.intercept(chain);

        verify(chain, never()).withReadTimeout(anyInt(), any());
        verify(chain, never()).withWriteTimeout(anyInt(), any());
        verify(chain).proceed(request);
    }
}
