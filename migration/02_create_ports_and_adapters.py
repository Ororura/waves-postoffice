#!/usr/bin/env python3
"""Add repository ports and ContractState adapters without changing stored key layout."""
from pathlib import Path
R=Path('contract/src/main/java/com/ororura')
assert (R/'api/PostContract.java').is_file(), 'Run stage 01 first'
assert not (R/'domain/repository/UserRepository.java').exists(), 'Stage 02 already applied'
files={
'domain/model/UserRole.java': '''package com.ororura.domain.model;
public final class UserRole {
    private UserRole() {}
    public static final String USER = "USER";
    public static final String EMPLOYEE = "EMPLOYEE";
}
''',
'domain/model/ParcelType.java': '''package com.ororura.domain.model;
public final class ParcelType {
    private ParcelType() {}
    public static final String LETTER = "LETTER";
    public static final String BANDEROLKA = "BANDEROLKA";
    public static final String PARCEL = "PARCEL";
}
''',
'domain/repository/UserRepository.java': '''package com.ororura.domain.repository;
import com.ororura.domain.model.User;
import java.util.Optional;
public interface UserRepository {
    Optional<User> findByAddress(String address);
    void save(User user);
}
''',
'domain/repository/ParcelRepository.java': '''package com.ororura.domain.repository;
import com.ororura.domain.model.Parcel;
import java.util.List;
public interface ParcelRepository {
    List<Parcel> findAll();
    void saveAll(List<Parcel> parcels);
}
''',
'domain/repository/TransferRepository.java': '''package com.ororura.domain.repository;
import com.ororura.domain.model.MoneyTransfer;
import java.util.List;
public interface TransferRepository {
    List<MoneyTransfer> findAll();
    void saveAll(List<MoneyTransfer> transfers);
}
''',
'domain/repository/PostOfficeRepository.java': '''package com.ororura.domain.repository;
import com.ororura.domain.model.PostOffice;
import java.util.HashMap;
public interface PostOfficeRepository {
    HashMap<Integer, PostOffice> findAll();
    void saveAll(HashMap<Integer, PostOffice> offices);
}
''',
'domain/repository/ContractMetadataRepository.java': '''package com.ororura.domain.repository;
import java.util.Optional;
public interface ContractMetadataRepository {
    Optional<String> findOwner();
    void saveOwner(String address);
}
''',
'application/context/ContractContext.java': '''package com.ororura.application.context;
public interface ContractContext {
    String caller();
}
''',
'infrastructure/blockchain/WavesContractContext.java': '''package com.ororura.infrastructure.blockchain;
import com.ororura.application.context.ContractContext;
import com.wavesenterprise.sdk.contract.api.domain.ContractCall;
public final class WavesContractContext implements ContractContext {
    private final ContractCall call;
    public WavesContractContext(ContractCall call) { this.call = call; }
    @Override public String caller() { return call.getCaller(); }
}
''',
'infrastructure/persistence/ContractStateUserRepository.java': '''package com.ororura.infrastructure.persistence;
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
''',
'infrastructure/persistence/ContractStateParcelRepository.java': '''package com.ororura.infrastructure.persistence;
import com.ororura.domain.model.Parcel;
import com.ororura.domain.repository.ParcelRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.List;
public final class ContractStateParcelRepository implements ParcelRepository {
    private final Mapping<List<Parcel>> mapping;
    public ContractStateParcelRepository(ContractState state) {
        mapping = state.getMapping(new TypeReference<List<Parcel>>() {}, "PARCEL_MAPPING");
    }
    @Override public List<Parcel> findAll() {
        return mapping.tryGet("_").orElseThrow(() ->
            new IllegalStateException("Посылки не инициализированы"));
    }
    @Override public void saveAll(List<Parcel> parcels) {
        mapping.put("_", parcels);
    }
}
''',
'infrastructure/persistence/ContractStateTransferRepository.java': '''package com.ororura.infrastructure.persistence;
import com.ororura.domain.model.MoneyTransfer;
import com.ororura.domain.repository.TransferRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.List;
public final class ContractStateTransferRepository implements TransferRepository {
    private final Mapping<List<MoneyTransfer>> mapping;
    public ContractStateTransferRepository(ContractState state) {
        mapping = state.getMapping(new TypeReference<List<MoneyTransfer>>() {}, "TRANSFER_MONEY_MAPPING");
    }
    @Override public List<MoneyTransfer> findAll() {
        return mapping.tryGet("_").orElseThrow(() ->
            new IllegalStateException("Переводы не инициализированы"));
    }
    @Override public void saveAll(List<MoneyTransfer> transfers) {
        mapping.put("_", transfers);
    }
}
''',
'infrastructure/persistence/ContractStatePostOfficeRepository.java': '''package com.ororura.infrastructure.persistence;
import com.ororura.domain.model.PostOffice;
import com.ororura.domain.repository.PostOfficeRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.HashMap;
public final class ContractStatePostOfficeRepository implements PostOfficeRepository {
    private final Mapping<HashMap<Integer, PostOffice>> mapping;
    public ContractStatePostOfficeRepository(ContractState state) {
        mapping = state.getMapping(new TypeReference<HashMap<Integer, PostOffice>>() {}, "OFFICE_MAPPING");
    }
    @Override public HashMap<Integer, PostOffice> findAll() {
        return mapping.tryGet("_").orElseThrow(() ->
            new IllegalStateException("Отделения не инициализированы"));
    }
    @Override public void saveAll(HashMap<Integer, PostOffice> offices) {
        mapping.put("_", offices);
    }
}
''',
'infrastructure/persistence/ContractStateMetadataRepository.java': '''package com.ororura.infrastructure.persistence;
import com.ororura.domain.repository.ContractMetadataRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.Optional;
public final class ContractStateMetadataRepository implements ContractMetadataRepository {
    private final Mapping<String> mapping;
    public ContractStateMetadataRepository(ContractState state) {
        mapping = state.getMapping(new TypeReference<String>() {}, "CONTRACT_META");
    }
    @Override public Optional<String> findOwner() { return mapping.tryGet("OWNER"); }
    @Override public void saveOwner(String address) { mapping.put("OWNER", address); }
}
''',
}
for name, s in files.items():
    p=R/name; p.parent.mkdir(parents=True, exist_ok=True); p.write_text(s)
user=R/'domain/model/User.java'; s=user.read_text().replace('import static com.ororura.api.IPostContract.Role.USER;', 'import static com.ororura.domain.model.UserRole.USER;');user.write_text(s)
price=R/'domain/pricing/CalculateTotalCost.java';s=price.read_text();s=s.replace('import static com.ororura.api.IPostContract.ParcelType.PARCEL;', 'import static com.ororura.domain.model.ParcelType.PARCEL;').replace('import static com.ororura.api.IPostContract.ParcelType.LETTER;', 'import static com.ororura.domain.model.ParcelType.LETTER;').replace('import static com.ororura.api.IPostContract.ParcelType.BANDEROLKA;', 'import static com.ororura.domain.model.ParcelType.BANDEROLKA;').replace('import static com.ororura.api.IPostContract.ParcelType.*;', 'import static com.ororura.domain.model.ParcelType.*;');price.write_text(s)
print('Stage 2 done. Repository ports + ContractState adapters; state keys unchanged.')
