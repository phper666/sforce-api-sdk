package io.github.phper666.sforce.api.sdk.auth;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.Session;

/**
 * @author Yuzhao.Li
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
