package com.ororura.infrastructure.persistence;

import com.ororura.domain.model.PostOffice;
import com.ororura.domain.repository.PostOfficeRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.HashMap;

public final class ContractStatePostOfficeRepository implements PostOfficeRepository {
  private final Mapping<HashMap<Integer, PostOffice>> mapping;

  public ContractStatePostOfficeRepository(ContractState state) {
    mapping =
        state.getMapping(new TypeReference<HashMap<Integer, PostOffice>>() {}, "OFFICE_MAPPING");
  }

  @Override
  public HashMap<Integer, PostOffice> findAll() {
    return mapping
        .tryGet("_")
        .orElseThrow(() -> new IllegalStateException("Отделения не инициализированы"));
  }

  @Override
  public void saveAll(HashMap<Integer, PostOffice> offices) {
    mapping.put("_", offices);
  }
}
