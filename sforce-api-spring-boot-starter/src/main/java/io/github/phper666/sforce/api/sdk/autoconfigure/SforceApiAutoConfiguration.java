package io.github.phper666.sforce.api.sdk.autoconfigure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for SforceApi beans.
 *
 * @author Yuzhao.Li
 */
@Configuration
@EnableConfigurationProperties(SforceApiProperties.class)
public class SforceApiAutoConfiguration {

    @Bean
    public SforceApiFactory sforceApiFactory(SforceApiProperties properties) {
        return new SforceApiFactory(properties);
    }
}
