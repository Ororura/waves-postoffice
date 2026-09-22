package com.ororura.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.ororura.domain.pricing.ShippingCostCalculator;
import org.junit.jupiter.api.Test;

class MoneyUnitsTest {
  @Test
  void creditsDebitAndRejectsOverflow() {
    User account = User.register("alice", "Alice", "Home");
    account.credit(125);
    assertEquals(125, account.getBalance());
    account.debit(25);
    assertEquals(100, account.getBalance());
    account.setBalance(Long.MAX_VALUE);
    assertThrows(ArithmeticException.class, () -> account.credit(1));
  }

  @Test
  void shippingIncludesInsuranceInMinorUnits() {
    Parcel parcel = new Parcel();
    parcel.setType(com.ororura.domain.model.ParcelType.PARCEL);
    parcel.setWeight(2.5);
    parcel.setDeclaredValue(100);
    assertEquals(135, ShippingCostCalculator.calculateTotalCost(parcel));
  }
}
