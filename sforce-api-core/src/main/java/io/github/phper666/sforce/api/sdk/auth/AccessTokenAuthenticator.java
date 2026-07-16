package io.github.phper666.sforce.api.sdk.auth;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.Session;

/**
 * Authenticator for OAuth 2.0 access token flow.
 *
 * @author Yuzhao.Li
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
