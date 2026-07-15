package com.phper666.sforce.api.sdk.config;

import java.util.concurrent.TimeUnit;

public final class SdkTypes {

    private SdkTypes() {}

    public enum HttpMethod {
        GET, POST, PATCH, DELETE, PUT
    }

    public enum ApiVersion {
        V56("v56.0"), V57("v57.0"), V58("v58.0"), V59("v59.0"),
        V60("v60.0"), V61("v61.0"), V62("v62.0"),
        DEFAULT_VERSION("v62.0");

        private final String version;
        ApiVersion(String version) { this.version = version; }
        public String version() { return version; }
    }

    public static class TimeoutSettings {
        private Integer timeOut;
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        public Integer getTimeOut() { return timeOut; }
        public void setTimeOut(Integer timeOut) { this.timeOut = timeOut; }
        public TimeUnit getTimeUnit() { return timeUnit; }
        public void setTimeUnit(TimeUnit timeUnit) { this.timeUnit = timeUnit; }
    }
}
