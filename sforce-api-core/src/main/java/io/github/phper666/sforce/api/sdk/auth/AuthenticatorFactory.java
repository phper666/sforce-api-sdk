package io.github.phper666.sforce.api.sdk.auth;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.exception.ConfigException;

/**
 * Factory for creating Salesforce authenticators based on SDK configuration.
 *
 * @author Yuzhao.Li
 */
public class AuthenticatorFactory {
    public static BaseAuthenticator create(SdkConfig config) {
        if (config.getAuthFlow() == null) {
            throw new ConfigException("invalid auth flow");
        }
        return switch (config.getAuthFlow()) {
            case CLIENT_CREDENTIAL -> new ClientCredentialsAuthenticator(config);
            case PASSWORD -> new PasswordAuthenticator(config);
            case AUTHORIZATION_CODE -> new AuthorizationCodeAuthenticator(config);
            case ACCESS_TOKEN -> new AccessTokenAuthenticator(config);
        };
    }
}
