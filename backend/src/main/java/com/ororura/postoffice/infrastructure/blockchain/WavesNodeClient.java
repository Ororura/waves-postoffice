package com.ororura.postoffice.infrastructure.blockchain;

import com.fasterxml.jackson.databind.JsonNode;
import com.ororura.postoffice.config.BlockchainProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class WavesNodeClient {
    private final RestClient client;
    private final BlockchainProperties properties;

    public WavesNodeClient(RestClient wavesRestClient, BlockchainProperties properties) {
        this.client = wavesRestClient;
        this.properties = properties;
    }

    public long height() {
        JsonNode response = client.get().uri("/blocks/height").retrieve().body(JsonNode.class);
        if (response == null || !response.path("height").canConvertToLong()) {
            throw new InvalidNodeResponseException();
        }
        return response.path("height").asLong();
    }

    public JsonNode contractInfo() {
        return requireBody(client.get().uri("/contracts/info/{id}", contractId())
                .retrieve().body(JsonNode.class));
    }

    public JsonNode contractState() {
        JsonNode state = requireBody(client.get().uri("/contracts/{id}", contractId())
                .retrieve().body(JsonNode.class));
        if (!state.isArray()) {
            throw new InvalidNodeResponseException();
        }
        return state;
    }

    public JsonNode transactionStatus(String transactionId) {
        try {
            JsonNode status = requireBody(client.get()
                    .uri("/contracts/status/{id}", transactionId)
                    .retrieve().body(JsonNode.class));
            if (!status.isArray()) {
                throw new InvalidNodeResponseException();
            }
            return status;
        } catch (HttpClientErrorException.NotFound exception) {
            // A newly broadcast transaction may not have a contract execution status yet.
            // This does not prove that the transaction exists or will be successful.
            throw new TransactionStatusNotAvailableException();
        }
    }

    private String contractId() {
        String id = properties.contractId();
        if (id == null || id.isBlank()) {
            throw new MissingContractIdException();
        }
        return id;
    }

    private JsonNode requireBody(JsonNode node) {
        if (node == null) {
            throw new InvalidNodeResponseException();
        }
        return node;
    }
}
