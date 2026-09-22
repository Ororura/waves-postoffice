package com.ororura.postoffice.infrastructure.blockchain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ororura.postoffice.api.dto.Actor;
import com.ororura.postoffice.api.dto.TransactionAccepted;
import com.ororura.postoffice.config.BlockchainProperties;
import java.net.URI;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class WavesTransactionClient {
    private final BlockchainProperties properties;
    private final WavesNodeClient node;
    private final ObjectMapper mapper;
    private final RestClient ownerClient;
    private final RestClient recipientClient;

    public WavesTransactionClient(
            BlockchainProperties properties,
            WavesNodeClient node,
            ObjectMapper mapper,
            RestClient.Builder builder) {
        this.properties = properties;
        this.node = node;
        this.mapper = mapper;
        this.ownerClient = createClient(builder.clone(), properties.ownerNodeUrl(), properties.apiKey());
        this.recipientClient = createClient(builder.clone(), properties.recipientNodeUrl(), properties.apiKey());
    }

    private static RestClient createClient(RestClient.Builder builder, URI url, String apiKey) {
        if (url == null) {
            throw new IllegalArgumentException("Sandbox node URL is missing");
        }
        builder.baseUrl(url.toString());
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-API-Key", apiKey);
        }
        return builder.build();
    }

    public TransactionAccepted call(Actor actor, String action, List<ContractParameter> params) {
        if (actor == null) {
            throw new IllegalArgumentException("Actor is required");
        }
        String contractId = properties.contractId();
        if (contractId == null || contractId.isBlank()) {
            throw new MissingContractIdException();
        }
        String sender = actor == Actor.OWNER ? properties.ownerAddress() : properties.recipientAddress();
        String password = actor == Actor.OWNER ? properties.ownerPassword() : properties.recipientPassword();
        if (sender == null || sender.isBlank() || password == null
                || "__UNCONFIGURED__".equals(password)) {
            throw new SignerNotConfiguredException();
        }

        // Read current version on every call: contract 107 updates it without changing contract ID.
        JsonNode info = node.contractInfo();
        int contractVersion = info.path("version").asInt(-1);
        if (contractVersion < 1) {
            throw new InvalidNodeResponseException();
        }

        ObjectNode tx = mapper.createObjectNode();
        tx.put("type", 104);
        tx.put("version", 2); // transaction version, NOT contract version
        tx.put("contractVersion", contractVersion);
        tx.put("contractId", contractId);
        tx.put("sender", sender);
        tx.put("password", password);
        tx.put("fee", 0); // only valid for this configured local sandbox
        tx.putNull("feeAssetId");
        ArrayNode txParams = tx.putArray("params");
        txParams.add(mapper.valueToTree(ContractParameter.string("action", action)));
        params.forEach(p -> txParams.add(mapper.valueToTree(p)));

        RestClient client = actor == Actor.OWNER ? ownerClient : recipientClient;
        JsonNode response;
        try {
            response = client.post()
                    .uri("/transactions/signAndBroadcast")
                    .body(tx)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            String reason = "Blockchain node rejected the transaction";
            try {
                JsonNode error = mapper.readTree(exception.getResponseBodyAsString());
                if (error.hasNonNull("message")) {
                    // Never echo the whole upstream payload: it can contain proofs and signed data.
                    reason = error.path("message").asText();
                }
            } catch (Exception ignored) {
                // No sensitive raw HTTP response in client-facing errors.
            }
            if (reason.length() > 350) {
                reason = reason.substring(0, 350);
            }
            throw new WavesTransactionRejectedException(reason);
        }
        if (response == null || !response.path("id").isTextual()
                || response.path("id").asText().isBlank()) {
            throw new InvalidNodeResponseException();
        }
        String txId = response.path("id").asText();
        return new TransactionAccepted(
                txId, contractId, action, "/api/v1/transactions/" + txId + "/status");
    }
}
