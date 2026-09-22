#!/usr/bin/env python3
"""Minimal JUnit tests + GitHub Actions for the layered contract."""
from pathlib import Path
R=Path('contract/src/test/java/com/ororura')
assert Path('contract/src/main/java/com/ororura/application/service/UserService.java').exists(), 'Run 03 first'
files={
'application/service/UserServiceTest.java': '''package com.ororura.application.service;
import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.User;
import com.ororura.domain.model.UserRole;
import com.ororura.domain.model.PostOffice;
import com.ororura.domain.repository.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class UserServiceTest {
    private final Map<String, User> store = new HashMap<>();
    private final UserRepository users = new UserRepository() {
        public Optional<User> findByAddress(String address) {
            return Optional.ofNullable(store.get(address));
        }
        public void save(User user) { store.put(user.getBlockchainAddress(), user); }
    };
    private final ContractMetadataRepository metadata = new ContractMetadataRepository() {
        public Optional<String> findOwner() { return Optional.of("owner"); }
        public void saveOwner(String address) { }
    };
    private final PostOfficeRepository offices = new PostOfficeRepository() {
        public HashMap<Integer, PostOffice> findAll() { return new HashMap<>(); }
        public void saveAll(HashMap<Integer, PostOffice> value) { }
    };
    private final UserService service = new UserService(users, new PostOfficeService(offices),
        () -> "alice", new AccessPolicy(metadata, () -> "alice"));
    @Test void registerUsesCallerAndIgnoresUntrustedBalanceAndRole() {
        User request = new User();
        request.setName("Alice");
        request.setBlockchainAddress("someone-else");
        request.setBalance(999999);
        request.setRole(UserRole.EMPLOYEE);
        service.createUser(request);
        User stored = store.get("alice");
        assertNotNull(stored);
        assertEquals("alice", stored.getBlockchainAddress());
        assertEquals(0.0, stored.getBalance());
        assertEquals(UserRole.USER, stored.getRole());
        assertNull(store.get("someone-else"));
    }
    @Test void changeDataPreservesAddressRoleAndBalance() {
        User stored = new User();
        stored.setName("Alice");
        stored.setBlockchainAddress("alice");
        stored.setRole(UserRole.USER);
        stored.setBalance(150);
        store.put("alice", stored);
        User update = new User();
        update.setName("New name");
        update.setBlockchainAddress("attacker");
        update.setBalance(100000);
        update.setRole(UserRole.EMPLOYEE);
        service.changePersonalData(update);
        assertEquals("New name", stored.getName());
        assertEquals("alice", stored.getBlockchainAddress());
        assertEquals(150.0, stored.getBalance());
        assertEquals(UserRole.USER, stored.getRole());
    }
}
''',
'application/service/TransferServiceTest.java': '''package com.ororura.application.service;
import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.*;
import com.ororura.domain.repository.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class TransferServiceTest {
    @Test void acceptanceDebitsSenderAndCannotBeRepeated() {
        Map<String, User> accounts=new HashMap<>();
        User alice=new User(); alice.setBlockchainAddress("alice"); alice.setName("Alice"); alice.setBalance(100);
        User bob=new User(); bob.setBlockchainAddress("bob"); bob.setName("Bob"); bob.setBalance(5);
        accounts.put("alice",alice); accounts.put("bob",bob);
        UserRepository users=new UserRepository() {
            public Optional<User> findByAddress(String address) { return Optional.ofNullable(accounts.get(address)); }
            public void save(User user) { accounts.put(user.getBlockchainAddress(), user); }
        };
        List<MoneyTransfer> data=new ArrayList<>();
        MoneyTransfer transfer=new MoneyTransfer();
        transfer.setFrom("alice"); transfer.setTo("bob"); transfer.setAmount(20); transfer.setActive(true);
        data.add(transfer);
        TransferRepository transfers=new TransferRepository() {
            public List<MoneyTransfer> findAll() { return data; }
            public void saveAll(List<MoneyTransfer> all) { List<MoneyTransfer> copy=new ArrayList<>(all); data.clear(); data.addAll(copy); }
        };
        ContractMetadataRepository metadata=new ContractMetadataRepository() {
            public Optional<String> findOwner() { return Optional.of("admin"); }
            public void saveOwner(String ignored) { }
        };
        PostOfficeRepository officeRepo=new PostOfficeRepository() {
            public HashMap<Integer, PostOffice> findAll() { return new HashMap<>(); }
            public void saveAll(HashMap<Integer, PostOffice> ignored) { }
        };
        UserService userService=new UserService(users, new PostOfficeService(officeRepo),
            () -> "bob", new AccessPolicy(metadata, () -> "bob"));
        TransferService service=new TransferService(transfers, users, userService, () -> "bob");
        service.acceptTransfer(0);
        assertEquals(80.0, alice.getBalance());
        assertEquals(25.0, bob.getBalance());
        assertFalse(data.get(0).isActive());
        assertThrows(IllegalStateException.class, () -> service.acceptTransfer(0));
        assertEquals(80.0, alice.getBalance());
        assertEquals(25.0, bob.getBalance());
    }
}
''',
}
for name,s in files.items():
    p=R/name;p.parent.mkdir(parents=True,exist_ok=True)
    if p.exists(): raise SystemExit('Existing test would be overwritten: '+str(p))
    p.write_text(s)
build=Path('contract/build.gradle.kts'); s=build.read_text()
if 'org.junit.jupiter:junit-jupiter' not in s:
    s=s.replace('dependencies {','dependencies {\n    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")',1)
if 'useJUnitPlatform()' not in s:
    s+='\n\ntasks.test {\n    useJUnitPlatform()\n}\n'
build.write_text(s)
p=Path('.github/workflows/contract-ci.yml');p.parent.mkdir(parents=True,exist_ok=True)
if not p.exists():
    p.write_text('''name: Contract CI
on:
  push:
    branches: [main, develop]
  pull_request:
permissions:
  contents: read
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v4
      - name: Test and build
        run: ./gradlew :contract:clean :contract:build --no-daemon
''')
print('Stage 4 done. JUnit 5 tests and CI added.')
