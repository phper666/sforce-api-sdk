package com.phper666.sforce.api.sdk.auth;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.Session;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Slf4j
public class ClientCredentialsAuthenticator extends BaseAuthenticator {
    public ClientCredentialsAuthenticator(SdkConfig apiConfig) {
        super(apiConfig);
    }

    @Override
    public Session login() {
        var formBody = new FormBody.Builder()
                .addEncoded("grant_type", "client_credentials")
                .addEncoded("client_id", config.getClientId())
                .addEncoded("client_secret", config.getClientSecret())
                .build();
        requestAccessToken(formBody, config.getLoginEndpoint());
        log.debug("Client credential login successful for domain: {}", config.getLoginEndpoint());
        return session;
    }

    @Override
    public Session refresh() {
        return login();
    }
}
