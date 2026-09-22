package com.ororura.application.service;

import static org.junit.jupiter.api.Assertions.*;

import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.*;
import com.ororura.domain.repository.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class TransferServiceTest {
  @Test
  void acceptanceDebitsSenderAndCannotBeRepeated() {
    Map<String, User> accounts = new HashMap<>();
    User alice = User.register("alice", "Alice", "home");
    alice.credit(100);
    User bob = User.register("bob", "Bob", "home");
    bob.credit(5);
    accounts.put("alice", alice);
    accounts.put("bob", bob);
    UserRepository users =
        new UserRepository() {
          public Optional<User> findByAddress(String id) {
            return Optional.ofNullable(accounts.get(id));
          }

          public void save(User user) {
            accounts.put(user.getBlockchainAddress(), user);
          }
        };
    Map<Integer, MoneyTransfer> stored = new HashMap<>();
    stored.put(0, MoneyTransfer.create("alice", "bob", 20, 1));
    TransferRepository transfers =
        new TransferRepository() {
          public int create(MoneyTransfer t) {
            int id = stored.size();
            stored.put(id, t);
            return id;
          }

          public MoneyTransfer requireById(int id) {
            return Optional.ofNullable(stored.get(id)).orElseThrow();
          }

          public void save(int id, MoneyTransfer t) {
            stored.put(id, t);
          }
        };
    ContractMetadataRepository metadata =
        new ContractMetadataRepository() {
          public Optional<String> findOwner() {
            return Optional.of("admin");
          }

          public void saveOwner(String address) {}
        };
    PostOfficeRepository offices =
        new PostOfficeRepository() {
          public HashMap<Integer, PostOffice> findAll() {
            return new HashMap<>();
          }

          public void saveAll(HashMap<Integer, PostOffice> values) {}
        };
    UserService userService =
        new UserService(
            users,
            new PostOfficeService(offices),
            () -> "bob",
            new AccessPolicy(metadata, () -> "bob"));
    TransferService service = new TransferService(transfers, users, userService, () -> "bob");
    service.acceptTransfer(0);
    assertEquals(80, accounts.get("alice").getBalance());
    assertEquals(25, accounts.get("bob").getBalance());
    assertEquals(TransferStatus.ACCEPTED, stored.get(0).getStatus());
    assertThrows(IllegalStateException.class, () -> service.acceptTransfer(0));
  }
}
