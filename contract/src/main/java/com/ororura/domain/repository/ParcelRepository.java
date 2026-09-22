package com.ororura.domain.repository;

import com.ororura.domain.model.Parcel;
import java.util.List;

public interface ParcelRepository {
  List<Parcel> findAll();

  boolean existsByTrackingNumber(String trackingNumber);

  void saveAll(List<Parcel> parcels);
}
