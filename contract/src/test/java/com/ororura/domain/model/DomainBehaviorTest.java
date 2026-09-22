package com.ororura.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DomainBehaviorTest {
  @Test
  void userCannotDebitNegativeAmountOrGoIntoDebt() {
    User user = User.register("alice", "Alice", "Home");
    user.credit(10.0);
    assertThrows(IllegalArgumentException.class, () -> user.debit(-1.0));
    assertThrows(IllegalStateException.class, () -> user.debit(11.0));
    assertEquals(10.0, user.getBalance());
    user.debit(4.0);
    assertEquals(6.0, user.getBalance());
  }

  @Test
  void profileAndEmploymentChangesKeepUserStateConsistent() {
    User user = User.register("alice", "Alice", "Home");
    user.assignToOffice(347901);
    assertEquals(UserRole.EMPLOYEE, user.getRole());
    assertEquals("RR347901", user.getPostId());
    user.changeProfile("New name", "New address");
    assertEquals("alice", user.getBlockchainAddress());
    user.removeFromOffice();
    assertEquals(UserRole.USER, user.getRole());
    assertNull(user.getPostId());
  }

  @Test
  void transferCannotBeHandledTwice() {
    MoneyTransfer transfer = MoneyTransfer.create("alice", "bob", 5.0, 1);
    transfer.requireRecipient("bob");
    assertThrows(SecurityException.class, () -> transfer.requireRecipient("alice"));
    transfer.accept();
    assertThrows(IllegalStateException.class, transfer::accept);
    assertThrows(IllegalStateException.class, transfer::reject);
  }

  @Test
  void invalidAmountsAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> User.requirePositiveFinite(0));
    assertThrows(IllegalArgumentException.class, () -> User.requirePositiveFinite(Double.NaN));
    assertThrows(
        IllegalArgumentException.class, () -> User.requirePositiveFinite(Double.POSITIVE_INFINITY));
  }
}
