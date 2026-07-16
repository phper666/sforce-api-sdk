package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.Session;
import com.phper666.sforce.api.sdk.config.AuthFlow;
import com.phper666.sforce.api.sdk.config.SdkTypes;
import com.phper666.sforce.api.sdk.config.SforceApiManager;

import com.phper666.sforce.api.sdk.auth.*;
import com.phper666.sforce.api.sdk.exception.ConfigException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthFlowFactoryTest {

    @Test
    void createClientCredentialFlow() {
        SdkConfig config = new SdkConfig().setAuthFlow(AuthFlow.CLIENT_CREDENTIAL);
        BaseAuthenticator flow = AuthenticatorFactory.create(config);
        assertInstanceOf(ClientCredentialsAuthenticator.class, flow);
    }

    @Test
    void createPasswordFlow() {
        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.PASSWORD)
                .setUsername("user")
                .setPassword("pass")
                .setClientId("id")
                .setClientSecret("secret");
        BaseAuthenticator flow = AuthenticatorFactory.create(config);
        assertInstanceOf(PasswordAuthenticator.class, flow);
    }

    @Test
    void createAuthorizationCodeFlow() {
        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.AUTHORIZATION_CODE)
                .setAuthorizationCode("code")
                .setRedirectUri("https://example.com/callback")
                .setClientId("id")
                .setClientSecret("secret");
        BaseAuthenticator flow = AuthenticatorFactory.create(config);
        assertInstanceOf(AuthorizationCodeAuthenticator.class, flow);
    }

    @Test
    void createAccessTokenFlow() {
        SdkConfig config = new SdkConfig()
                .setAuthFlow(AuthFlow.ACCESS_TOKEN)
                .setAccessToken("token")
                .setLoginEndpoint("https://domain.my.salesforce.com");
        BaseAuthenticator flow = AuthenticatorFactory.create(config);
        assertInstanceOf(AccessTokenAuthenticator.class, flow);
    }

    @Test
    void createWithNullAuthFlowThrowsConfigException() {
        SdkConfig config = new SdkConfig() {
            @Override
            public AuthFlow getAuthFlow() {
                return null;
            }
        };
        ConfigException exception = assertThrows(ConfigException.class, () -> AuthenticatorFactory.create(config));
        assertEquals("invalid auth flow", exception.getMessage());
    }
}
