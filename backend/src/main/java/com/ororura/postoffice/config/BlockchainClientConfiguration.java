package com.ororura.postoffice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(BlockchainProperties.class)
public class BlockchainClientConfiguration {
    @Bean
    RestClient wavesRestClient(RestClient.Builder builder, BlockchainProperties properties) {
        RestClient.Builder configured = builder.baseUrl(properties.nodeUrl().toString());
        if (properties.apiKey() != null && !properties.apiKey().isBlank()) {
            configured.defaultHeader("X-API-Key", properties.apiKey());
        }
        return configured.build();
    }
}
