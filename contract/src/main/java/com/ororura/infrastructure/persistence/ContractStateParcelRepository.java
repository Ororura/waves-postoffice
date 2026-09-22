package com.ororura.infrastructure.persistence;

import com.ororura.domain.model.Parcel;
import com.ororura.domain.repository.ParcelRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.Optional;

public final class ContractStateParcelRepository implements ParcelRepository {
  private final Mapping<Parcel> mapping;

  public ContractStateParcelRepository(ContractState state) {
    mapping = state.getMapping(new TypeReference<Parcel>() {}, "PARCEL_V2");
  }

  @Override
  public Optional<Parcel> findByTrackingNumber(String trackingNumber) {
    if (trackingNumber == null || trackingNumber.isBlank()) return Optional.empty();
    return mapping.tryGet(trackingNumber);
  }

  @Override
  public void save(Parcel parcel) {
    if (parcel == null || parcel.getTrackNumber() == null || parcel.getTrackNumber().isBlank()) {
      throw new IllegalArgumentException("Не указан трек-номер");
    }
    mapping.put(parcel.getTrackNumber(), parcel);
  }
}
