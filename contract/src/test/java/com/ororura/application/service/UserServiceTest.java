package com.ororura.application.service;
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
