package com.ororura.postoffice.config;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockchainPropertiesTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(RestClient.Builder.class, RestClient::builder)
            .withUserConfiguration(BlockchainClientConfiguration.class)
            .withPropertyValues(
                    "blockchain.node-url=http://127.0.0.1:6862",
                    "blockchain.contract-id=En8e514ghDoNHuRJ11Bc5pW1tNXW1ajmcoGBvkRjTQix",
                    "blockchain.api-key=");

    @Test
    void bindsNodeAndContractId() {
        runner.run(context -> {
            BlockchainProperties properties = context.getBean(BlockchainProperties.class);
            assertEquals(URI.create("http://127.0.0.1:6862"), properties.nodeUrl());
            assertEquals("En8e514ghDoNHuRJ11Bc5pW1tNXW1ajmcoGBvkRjTQix", properties.contractId());
        });
    }
}
