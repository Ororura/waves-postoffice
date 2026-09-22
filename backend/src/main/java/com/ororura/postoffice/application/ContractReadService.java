package com.ororura.postoffice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ororura.postoffice.api.dto.TransferResponse;
import com.ororura.postoffice.api.dto.UserResponse;
import com.ororura.postoffice.infrastructure.blockchain.InvalidNodeResponseException;
import com.ororura.postoffice.infrastructure.blockchain.WavesNodeClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ContractReadService {
    private final WavesNodeClient node;
    private final ObjectMapper mapper;

    public ContractReadService(WavesNodeClient node, ObjectMapper mapper) {
        this.node = node;
        this.mapper = mapper;
    }

    public List<UserResponse> users() {
        List<UserResponse> result = new ArrayList<>();
        for (JsonNode entry : node.contractState()) {
            if (key(entry).startsWith("USERS_MAPPING_")) {
                result.add(read(entry, UserResponse.class));
            }
        }
        result.sort(Comparator.comparing(UserResponse::blockchainAddress));
        return result;
    }

    public Optional<UserResponse> user(String address) {
        return users().stream()
                .filter(user -> address.equals(user.blockchainAddress()))
                .findFirst();
    }

    public List<TransferResponse> transfers() {
        List<TransferResponse> result = new ArrayList<>();
        for (JsonNode entry : node.contractState()) {
            String key = key(entry);
            if (key.startsWith("TRANSFER_V2_") && key.substring("TRANSFER_V2_".length()).matches("\\d+")) {
                int id = Integer.parseInt(key.substring("TRANSFER_V2_".length()));
                JsonNode value = read(entry, JsonNode.class);
                result.add(new TransferResponse(
                        id,
                        value.path("from").asText(),
                        value.path("to").asText(),
                        value.path("amount").asLong(),
                        value.path("lifeTime").asInt(),
                        value.path("status").asText()));
            }
        }
        result.sort(Comparator.comparingInt(TransferResponse::id));
        return result;
    }

    public List<JsonNode> offices() {
        for (JsonNode entry : node.contractState()) {
            if ("OFFICE_MAPPING__".equals(key(entry))) {
                JsonNode offices = read(entry, JsonNode.class);
                if (!offices.isObject()) {
                    throw new InvalidNodeResponseException();
                }
                List<JsonNode> result = new ArrayList<>();
                offices.elements().forEachRemaining(result::add);
                result.sort(Comparator.comparingInt(o -> o.path("postNumber").asInt()));
                return result;
            }
        }
        return List.of();
    }

    private String key(JsonNode entry) {
        return entry.path("key").asText();
    }

    private <T> T read(JsonNode entry, Class<T> type) {
        try {
            return mapper.readValue(entry.path("value").asText(), type);
        } catch (JsonProcessingException exception) {
            throw new InvalidNodeResponseException();
        }
    }
}
