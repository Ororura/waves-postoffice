package com.ororura.application.service;

import static org.junit.jupiter.api.Assertions.*;

import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.*;
import com.ororura.domain.repository.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class TransferPersistenceTest {
  private static User copy(User value) {
    User user =
        User.register(value.getBlockchainAddress(), value.getName(), value.getHomeAddress());
    user.setBalance(value.getBalance());
    user.setRole(value.getRole());
    user.setPostId(value.getPostId());
    return user;
  }

  private static MoneyTransfer copy(MoneyTransfer value) {
    MoneyTransfer t = new MoneyTransfer();
    t.setFrom(value.getFrom());
    t.setTo(value.getTo());
    t.setAmount(value.getAmount());
    t.setLifeTime(value.getLifeTime());
    t.setStatus(value.getStatus());
    return t;
  }

  @Test
  void transferRequiresExplicitSaves() {
    Map<String, User> store = new HashMap<>();
    User alice = User.register("alice", "Alice", "home");
    alice.credit(100);
    User bob = User.register("bob", "Bob", "home");
    bob.credit(5);
    store.put("alice", copy(alice));
    store.put("bob", copy(bob));
    UserRepository users =
        new UserRepository() {
          public Optional<User> findByAddress(String id) {
            return Optional.ofNullable(store.get(id)).map(TransferPersistenceTest::copy);
          }

          public void save(User value) {
            store.put(value.getBlockchainAddress(), copy(value));
          }
        };
    Map<Integer, MoneyTransfer> transferStore = new HashMap<>();
    transferStore.put(0, MoneyTransfer.create("alice", "bob", 20, 1));
    TransferRepository transfers =
        new TransferRepository() {
          public int create(MoneyTransfer value) {
            int id = transferStore.size();
            transferStore.put(id, copy(value));
            return id;
          }

          public MoneyTransfer requireById(int id) {
            return Optional.ofNullable(transferStore.get(id))
                .map(TransferPersistenceTest::copy)
                .orElseThrow();
          }

          public void save(int id, MoneyTransfer value) {
            transferStore.put(id, copy(value));
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

          public void saveAll(HashMap<Integer, PostOffice> value) {}
        };
    UserService userService =
        new UserService(
            users,
            new PostOfficeService(offices),
            () -> "bob",
            new AccessPolicy(metadata, () -> "bob"));
    TransferService service = new TransferService(transfers, users, userService, () -> "bob");
    service.acceptTransfer(0);
    assertEquals(80, store.get("alice").getBalance());
    assertEquals(25, store.get("bob").getBalance());
    assertEquals(TransferStatus.ACCEPTED, transferStore.get(0).getStatus());
    assertThrows(IllegalStateException.class, () -> service.acceptTransfer(0));
  }
}
