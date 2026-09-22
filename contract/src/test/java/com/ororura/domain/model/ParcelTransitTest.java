package com.ororura.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ParcelTransitTest {
  @Test
  void recordsRouteOnceAndRejectsWrongOffice() {
    Parcel parcel = new Parcel();
    parcel.setTrackNumber("RR-123");
    parcel.setStatus(ParcelStatus.ACCEPTED);
    parcel.setNextOffice(347901);
    ParcelMovement transit = parcel.transferViaOffice(347901, 344000, "employee");
    assertEquals(1, transit.getSequence());
    assertEquals(344000, parcel.getNextOffice());
    assertEquals(ParcelStatus.IN_TRANSIT, parcel.getStatus());
    assertEquals(1, parcel.getTransitHistory().size());
    assertThrows(
        IllegalStateException.class, () -> parcel.transferViaOffice(347901, 347902, "employee"));
    assertEquals(1, parcel.getTransitHistory().size());
  }
}
