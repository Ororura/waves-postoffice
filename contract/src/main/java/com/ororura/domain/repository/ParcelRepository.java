package com.ororura.domain.repository;

import com.ororura.domain.model.Parcel;
import java.util.Optional;

public interface ParcelRepository {
  Optional<Parcel> findByTrackingNumber(String trackingNumber);

  default boolean existsByTrackingNumber(String trackingNumber) {
    return findByTrackingNumber(trackingNumber).isPresent();
  }

  void save(Parcel parcel);
}
