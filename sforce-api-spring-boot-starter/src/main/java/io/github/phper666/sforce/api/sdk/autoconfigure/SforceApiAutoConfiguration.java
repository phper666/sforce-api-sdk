package io.github.phper666.sforce.api.sdk.autoconfigure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Configuration
@EnableConfigurationProperties(SforceApiProperties.class)
public class SforceApiAutoConfiguration {

    @Bean
    public SforceApiFactory sforceApiFactory(SforceApiProperties properties) {
        return new SforceApiFactory(properties);
    }
}
