package com.phper666.sforce.api.sdk.config;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public enum AuthFlow {
    PASSWORD, //attention! PASSWORD auth flow only be used for test
    CLIENT_CREDENTIAL,
    AUTHORIZATION_CODE,
    ACCESS_TOKEN;
}
