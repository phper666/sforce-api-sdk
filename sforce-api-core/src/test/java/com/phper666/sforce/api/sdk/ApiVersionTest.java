package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.config.SdkTypes.ApiVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiVersionTest {

    @Test
    void defaultVersionIsV62() {
        assertEquals("v62.0", ApiVersion.DEFAULT_VERSION.version());
    }

    @Test
    void allVersionsReturnExpectedStrings() {
        assertEquals("v56.0", ApiVersion.V56.version());
        assertEquals("v57.0", ApiVersion.V57.version());
        assertEquals("v58.0", ApiVersion.V58.version());
        assertEquals("v59.0", ApiVersion.V59.version());
        assertEquals("v60.0", ApiVersion.V60.version());
        assertEquals("v61.0", ApiVersion.V61.version());
        assertEquals("v62.0", ApiVersion.V62.version());
    }

    @Test
    void defaultVersionEqualsV62Enum() {
        assertEquals(ApiVersion.V62.version(), ApiVersion.DEFAULT_VERSION.version());
    }
}
