package com.ororura.postoffice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ororura.postoffice.api.dto.Actor;
import com.ororura.postoffice.api.dto.ActorRequest;
import com.ororura.postoffice.api.dto.CreateTransferRequest;
import com.ororura.postoffice.api.dto.CreditRequest;
import com.ororura.postoffice.api.dto.RegisterUserRequest;
import com.ororura.postoffice.api.dto.TransactionAccepted;
import com.ororura.postoffice.api.dto.UpdateProfileRequest;
import com.ororura.postoffice.infrastructure.blockchain.ContractParameter;
import com.ororura.postoffice.infrastructure.blockchain.WavesTransactionClient;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ContractWriteService {
    private final WavesTransactionClient client;
    private final ObjectMapper mapper;

    public ContractWriteService(WavesTransactionClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public TransactionAccepted register(RegisterUserRequest request) {
        String user = json(Map.of("name", request.name(), "homeAddress", request.homeAddress()));
        return client.call(request.actor(), "createUser", List.of(ContractParameter.string("user", user)));
    }

    public TransactionAccepted updateProfile(UpdateProfileRequest request) {
        String user = json(Map.of("name", request.name(), "homeAddress", request.homeAddress()));
        return client.call(request.actor(), "changePersonalData", List.of(ContractParameter.string("user", user)));
    }

    public TransactionAccepted credit(String address, CreditRequest request) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address must not be empty");
        }
        return client.call(Actor.OWNER, "creditUser", List.of(
                ContractParameter.string("user", address),
                ContractParameter.integer("amount", request.amount())));
    }

    public TransactionAccepted transfer(CreateTransferRequest request) {
        String money = json(Map.of("to", request.to(), "amount", request.amount()));
        return client.call(request.actor(), "transferMoney", List.of(ContractParameter.string("money", money)));
    }

    public TransactionAccepted accept(int id, ActorRequest request) {
        return client.call(request.actor(), "acceptTransfer", List.of(ContractParameter.integer("id", id)));
    }

    public TransactionAccepted reject(int id, ActorRequest request) {
        return client.call(request.actor(), "deniedTransfer", List.of(ContractParameter.integer("id", id)));
    }

    private String json(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not encode contract argument", exception);
        }
    }
}
