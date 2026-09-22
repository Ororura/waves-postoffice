package com.ororura.infrastructure.persistence;
import com.ororura.domain.model.User;
import com.ororura.domain.repository.UserRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.Optional;
public final class ContractStateUserRepository implements UserRepository {
    private final Mapping<User> mapping;
    public ContractStateUserRepository(ContractState state) {
        mapping = state.getMapping(new TypeReference<User>() {}, "USERS_MAPPING");
    }
    @Override public Optional<User> findByAddress(String address) {
        return mapping.tryGet(address);
    }
    @Override public void save(User user) {
        mapping.put(user.getBlockchainAddress(), user);
    }
}
