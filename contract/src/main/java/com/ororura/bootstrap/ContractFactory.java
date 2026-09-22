package com.ororura.bootstrap;

import com.ororura.application.context.ContractContext;
import com.ororura.application.security.AccessPolicy;
import com.ororura.application.service.*;
import com.ororura.domain.repository.*;
import com.ororura.infrastructure.blockchain.WavesContractContext;
import com.ororura.infrastructure.persistence.*;
import com.wavesenterprise.sdk.contract.api.domain.ContractCall;
import com.wavesenterprise.sdk.contract.api.state.ContractState;

public final class ContractFactory {
  private final UserService userService;
  private final TransferService transferService;
  private final ParcelService parcelService;
  private final PostOfficeService officeService;
  private final InitializationService initializationService;

  public ContractFactory(ContractState state, ContractCall call) {
    ContractContext context = new WavesContractContext(call);
    UserRepository users = new ContractStateUserRepository(state);
    TransferRepository transfers = new ContractStateTransferRepository(state);
    ParcelRepository parcels = new ContractStateParcelRepository(state);
    PostOfficeRepository offices = new ContractStatePostOfficeRepository(state);
    ContractMetadataRepository metadata = new ContractStateMetadataRepository(state);
    AccessPolicy access = new AccessPolicy(metadata, context);
    officeService = new PostOfficeService(offices);
    userService = new UserService(users, officeService, context, access);
    transferService = new TransferService(transfers, users, userService, context);
    parcelService = new ParcelService(parcels, users, userService, officeService, context, access);
    initializationService =
        new InitializationService(metadata, parcels, transfers, offices, context);
  }

  public UserService users() {
    return userService;
  }

  public TransferService transfers() {
    return transferService;
  }

  public ParcelService parcels() {
    return parcelService;
  }

  public InitializationService initialization() {
    return initializationService;
  }
}
