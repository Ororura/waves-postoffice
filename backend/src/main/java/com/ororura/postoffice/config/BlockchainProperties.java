package com.ororura.postoffice.config;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blockchain")
public record BlockchainProperties(
        URI nodeUrl,
        String contractId,
        String apiKey,
        String ownerAddress,
        URI ownerNodeUrl,
        String ownerPassword,
        String recipientAddress,
        URI recipientNodeUrl,
        String recipientPassword) {}
