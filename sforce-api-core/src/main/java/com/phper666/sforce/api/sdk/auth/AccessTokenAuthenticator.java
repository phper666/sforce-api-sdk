package com.phper666.sforce.api.sdk.auth;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.Session;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public class AccessTokenAuthenticator extends BaseAuthenticator {
    public AccessTokenAuthenticator(SdkConfig apiConfig) {
        super(apiConfig);
    }

    @Override
    public Session login() {
        return new Session(config.getAccessToken(), config.getLoginEndpoint());
    }

    @Override
    public Session refresh() {
        return login();
    }
}
