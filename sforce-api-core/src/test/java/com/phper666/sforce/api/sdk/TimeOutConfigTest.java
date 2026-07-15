package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TimeOutConfigTest {

    @Test
    void defaultValues() {
        TimeoutSettings config = new TimeoutSettings();
        assertNull(config.getTimeOut());
        assertEquals(TimeUnit.MILLISECONDS, config.getTimeUnit());
    }

    @Test
    void setValues() {
        TimeoutSettings config = new TimeoutSettings();
        config.setTimeOut(30);
        config.setTimeUnit(TimeUnit.MILLISECONDS);
        assertEquals(30, config.getTimeOut());
        assertEquals(TimeUnit.MILLISECONDS, config.getTimeUnit());
    }

    @Test
    void settersOverridePreviousValues() {
        TimeoutSettings config = new TimeoutSettings();
        config.setTimeOut(10);
        config.setTimeUnit(TimeUnit.MINUTES);
        assertEquals(10, config.getTimeOut());
        assertEquals(TimeUnit.MINUTES, config.getTimeUnit());

        config.setTimeOut(60);
        config.setTimeUnit(TimeUnit.SECONDS);
        assertEquals(60, config.getTimeOut());
        assertEquals(TimeUnit.SECONDS, config.getTimeUnit());
    }
}
