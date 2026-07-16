package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.internal.DebugInterceptor;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggingInterceptorTest {

    @Test
    void constructorStoresSettings() {
        DebugInterceptor interceptor = new DebugInterceptor(true, 4096);
        assertNotNull(interceptor);

        DebugInterceptor disabled = new DebugInterceptor(false, 1024);
        assertNotNull(disabled);
    }

    @Test
    void interceptReturnsProceededResponse() throws Exception {
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Request request = new Request.Builder()
                .url("https://example.com/test")
                .build();
        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build();

        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);

        DebugInterceptor interceptor = new DebugInterceptor(false, 1024);
        Response actual = interceptor.intercept(chain);

        assertSame(response, actual);
        verify(chain).proceed(request);
    }

    @Test
    void interceptWithAuthorizationHeaderDoesNotThrow() throws Exception {
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Request request = new Request.Builder()
                .url("https://example.com/test")
                .addHeader("Authorization", "Bearer secret-token")
                .build();
        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build();

        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);

        DebugInterceptor interceptor = new DebugInterceptor(true, 4096);
        assertDoesNotThrow(() -> interceptor.intercept(chain));
    }

    @Test
    void truncateLimitsStringToMaxSize() throws Exception {
        Method truncate = DebugInterceptor.class.getDeclaredMethod("truncate", String.class);
        truncate.setAccessible(true);

        DebugInterceptor interceptor = new DebugInterceptor(true, 5);
        String result = (String) truncate.invoke(interceptor, "hello world");
        assertTrue(result.startsWith("hello"));
        assertTrue(result.contains("11 bytes"));
    }

    @Test
    void truncateReturnsNullForNullInput() throws Exception {
        Method truncate = DebugInterceptor.class.getDeclaredMethod("truncate", String.class);
        truncate.setAccessible(true);

        DebugInterceptor interceptor = new DebugInterceptor(true, 4096);
        assertNull(truncate.invoke(interceptor, new Object[]{null}));
    }
}
