package io.github.phper666.sforce.api.sdk.config;

/**
 * Enum for Salesforce OAuth authorization flows.
 *
 * @author Yuzhao.Li
 */
public enum AuthFlow {
    PASSWORD, //attention! PASSWORD auth flow only be used for test
    CLIENT_CREDENTIAL,
    AUTHORIZATION_CODE,
    ACCESS_TOKEN;
}
