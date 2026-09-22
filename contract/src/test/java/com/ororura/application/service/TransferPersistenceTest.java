package com.ororura.application.service;

import static org.junit.jupiter.api.Assertions.*;

import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.MoneyTransfer;
import com.ororura.domain.model.PostOffice;
import com.ororura.domain.model.User;
import com.ororura.domain.repository.ContractMetadataRepository;
import com.ororura.domain.repository.PostOfficeRepository;
import com.ororura.domain.repository.TransferRepository;
import com.ororura.domain.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TransferPersistenceTest {
  private static User copy(User source) {
    User user = new User();
    user.setBlockchainAddress(source.getBlockchainAddress());
    user.setName(source.getName());
    user.setHomeAddress(source.getHomeAddress());
    user.setBalance(source.getBalance());
    user.setRole(source.getRole());
    user.setPostId(source.getPostId());
    return user;
  }

  private static MoneyTransfer copy(MoneyTransfer source) {
    MoneyTransfer transfer = new MoneyTransfer();
    transfer.setFrom(source.getFrom());
    transfer.setTo(source.getTo());
    transfer.setAmount(source.getAmount());
    transfer.setLifeTime(source.getLifeTime());
    transfer.setActive(source.isActive());
    return transfer;
  }

  @Test
  void acceptingTransferExplicitlyPersistsBothBalancesAndTransferState() {
    Map<String, User> storedUsers = new HashMap<>();
    User alice = User.register("alice", "Alice", "Home");
    User bob = User.register("bob", "Bob", "Home");
    alice.credit(100.0);
    bob.credit(5.0);
    storedUsers.put("alice", copy(alice));
    storedUsers.put("bob", copy(bob));

    UserRepository users =
        new UserRepository() {
          @Override
          public Optional<User> findByAddress(String address) {
            return Optional.ofNullable(storedUsers.get(address)).map(TransferPersistenceTest::copy);
          }

          @Override
          public void save(User user) {
            storedUsers.put(user.getBlockchainAddress(), copy(user));
          }
        };

    List<MoneyTransfer> storedTransfers = new ArrayList<>();
    storedTransfers.add(MoneyTransfer.create("alice", "bob", 20.0, 1));
    TransferRepository transfers =
        new TransferRepository() {
          @Override
          public List<MoneyTransfer> findAll() {
            return storedTransfers.stream().map(TransferPersistenceTest::copy).toList();
          }

          @Override
          public void saveAll(List<MoneyTransfer> values) {
            storedTransfers.clear();
            values.stream().map(TransferPersistenceTest::copy).forEach(storedTransfers::add);
          }
        };

    ContractMetadataRepository metadata =
        new ContractMetadataRepository() {
          @Override
          public Optional<String> findOwner() {
            return Optional.of("admin");
          }

          @Override
          public void saveOwner(String address) {}
        };
    PostOfficeRepository officeRepo =
        new PostOfficeRepository() {
          @Override
          public HashMap<Integer, PostOffice> findAll() {
            return new HashMap<>();
          }

          @Override
          public void saveAll(HashMap<Integer, PostOffice> offices) {}
        };
    UserService userService =
        new UserService(
            users,
            new PostOfficeService(officeRepo),
            () -> "bob",
            new AccessPolicy(metadata, () -> "bob"));
    TransferService service = new TransferService(transfers, users, userService, () -> "bob");

    service.acceptTransfer(0);
    assertEquals(80.0, storedUsers.get("alice").getBalance());
    assertEquals(25.0, storedUsers.get("bob").getBalance());
    assertFalse(storedTransfers.get(0).isActive());
    assertThrows(IllegalStateException.class, () -> service.acceptTransfer(0));
  }
}
