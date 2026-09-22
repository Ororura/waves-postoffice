package com.ororura.infrastructure.persistence;

import com.ororura.domain.model.Parcel;
import com.ororura.domain.repository.ParcelRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.List;

public final class ContractStateParcelRepository implements ParcelRepository {
  private final Mapping<List<Parcel>> mapping;

  public ContractStateParcelRepository(ContractState state) {
    mapping = state.getMapping(new TypeReference<List<Parcel>>() {}, "PARCEL_MAPPING");
  }

  @Override
  public List<Parcel> findAll() {
    return mapping
        .tryGet("_")
        .orElseThrow(() -> new IllegalStateException("Посылки не инициализированы"));
  }

  @Override
  public boolean existsByTrackingNumber(String trackingNumber) {
    return findAll().stream().anyMatch(parcel -> trackingNumber.equals(parcel.getTrackNumber()));
  }

  @Override
  public void saveAll(List<Parcel> parcels) {
    mapping.put("_", parcels);
  }
}
