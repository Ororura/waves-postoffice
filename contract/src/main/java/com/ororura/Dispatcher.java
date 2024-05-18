package com.ororura;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ororura.app.PostContract;
import com.wavesenterprise.sdk.contract.core.dispatch.ContractDispatcher;
import com.wavesenterprise.sdk.contract.grpc.GrpcJacksonContractDispatcherBuilder;

public class Dispatcher {
    public static void main(String[] args) {
        ContractDispatcher contractDispatcher = GrpcJacksonContractDispatcherBuilder.builder()
                .contractHandlerType(PostContract.class)
                .objectMapper(new ObjectMapper())
                .build();

        contractDispatcher.dispatch();
    }
}