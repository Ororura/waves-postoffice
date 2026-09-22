package com.ororura.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ororura.api.WavesPostOfficeContract;
import com.wavesenterprise.sdk.contract.core.dispatch.ContractDispatcher;
import com.wavesenterprise.sdk.contract.grpc.GrpcJacksonContractDispatcherBuilder;

public class PostOfficeContractDispatcher {
  public static void main(String[] args) {
    ContractDispatcher contractDispatcher =
        GrpcJacksonContractDispatcherBuilder.builder()
            .contractHandlerType(WavesPostOfficeContract.class)
            .objectMapper(new ObjectMapper())
            .build();

    contractDispatcher.dispatch();
  }
}
