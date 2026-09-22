package com.ororura.domain.pricing;

import static org.junit.jupiter.api.Assertions.*;

import com.ororura.domain.model.Parcel;
import org.junit.jupiter.api.Test;

class CalculateTotalCostTest {

  @Test
  void shouldCalculateParcelCost() {
    Parcel parcel = new Parcel();

    parcel.setType(com.ororura.domain.model.ParcelType.PARCEL);
    parcel.setWeight(2.5);
    parcel.setDeclaredValue(0);

    long cost = CalculateTotalCost.calculateTotalCost(parcel);

    assertEquals(125, cost);
  }

  @Test
  void shouldRejectNegativeWeight() {
    Parcel parcel = new Parcel();

    parcel.setType(com.ororura.domain.model.ParcelType.PARCEL);
    parcel.setWeight(-10);

    assertThrows(
        IllegalArgumentException.class, () -> CalculateTotalCost.calculateTotalCost(parcel));
  }

  @Test
  void shouldRejectUnknownParcelType() {
    Parcel parcel = new Parcel();

    parcel.setType(null);
    parcel.setWeight(5);

    assertThrows(
        IllegalArgumentException.class, () -> CalculateTotalCost.calculateTotalCost(parcel));
  }
}
