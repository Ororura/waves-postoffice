package com.ororura.infrastructure.persistence;

import com.ororura.domain.repository.ContractMetadataRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.Optional;

public final class ContractStateMetadataRepository implements ContractMetadataRepository {

  private final Mapping<String> mapping;
  private final ContractState state;

  public ContractStateMetadataRepository(ContractState state) {
    this.state = state;
    mapping = state.getMapping(String.class, "CONTRACT_META");
  }

  @Override
  public Optional<String> findOwner() {
    return mapping.tryGet("OWNER");
  }

  @Override
  public void saveOwner(String address) {
    mapping.put("OWNER", address);
    state.put("CONTRACT_CALL", address);
  }
}
