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
    User alice = new User();
    alice.setBlockchainAddress("alice");
    alice.setName("Alice");
    alice.setBalance(100);
    User bob = new User();
    bob.setBlockchainAddress("bob");
    bob.setName("Bob");
    bob.setBalance(5);
    accounts.put("alice", alice);
    accounts.put("bob", bob);
    UserRepository users =
        new UserRepository() {
          public Optional<User> findByAddress(String address) {
            return Optional.ofNullable(accounts.get(address));
          }

          public void save(User user) {
            accounts.put(user.getBlockchainAddress(), user);
          }
        };
    List<MoneyTransfer> data = new ArrayList<>();
    MoneyTransfer transfer = new MoneyTransfer();
    transfer.setFrom("alice");
    transfer.setTo("bob");
    transfer.setAmount(20);
    transfer.setActive(true);
    data.add(transfer);
    TransferRepository transfers =
        new TransferRepository() {
          public List<MoneyTransfer> findAll() {
            return data;
          }

          public void saveAll(List<MoneyTransfer> all) {
            List<MoneyTransfer> copy = new ArrayList<>(all);
            data.clear();
            data.addAll(copy);
          }
        };
    ContractMetadataRepository metadata =
        new ContractMetadataRepository() {
          public Optional<String> findOwner() {
            return Optional.of("admin");
          }

          public void saveOwner(String ignored) {}
        };
    PostOfficeRepository officeRepo =
        new PostOfficeRepository() {
          public HashMap<Integer, PostOffice> findAll() {
            return new HashMap<>();
          }

          public void saveAll(HashMap<Integer, PostOffice> ignored) {}
        };
    UserService userService =
        new UserService(
            users,
            new PostOfficeService(officeRepo),
            () -> "bob",
            new AccessPolicy(metadata, () -> "bob"));
    TransferService service = new TransferService(transfers, users, userService, () -> "bob");
    service.acceptTransfer(0);
    assertEquals(80.0, alice.getBalance());
    assertEquals(25.0, bob.getBalance());
    assertFalse(data.get(0).isActive());
    assertThrows(IllegalStateException.class, () -> service.acceptTransfer(0));
    assertEquals(80.0, alice.getBalance());
    assertEquals(25.0, bob.getBalance());
  }
}
