package com.ororura.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DomainBehaviorTest {
  @Test
  void userCannotDebitNegativeAmountOrGoIntoDebt() {
    User user = User.register("alice", "Alice", "Home");
    user.credit(10);
    assertThrows(IllegalArgumentException.class, () -> user.debit(-1));
    assertThrows(IllegalStateException.class, () -> user.debit(11));
    assertEquals(10, user.getBalance());
    user.debit(4);
    assertEquals(6, user.getBalance());
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
    MoneyTransfer transfer = MoneyTransfer.create("alice", "bob", 5, 1);
    transfer.requireRecipient("bob");
    assertThrows(SecurityException.class, () -> transfer.requireRecipient("alice"));
    transfer.accept();
    assertThrows(IllegalStateException.class, transfer::accept);
    assertThrows(IllegalStateException.class, transfer::reject);
  }

  @Test
  void invalidAmountsAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> User.requirePositive(0));
    assertThrows(IllegalArgumentException.class, () -> User.requirePositive(-1));
    assertThrows(IllegalArgumentException.class, () -> User.requirePositive(Long.MIN_VALUE));
  }
}
