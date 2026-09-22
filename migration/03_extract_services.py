#!/usr/bin/env python3
"""Extract validated use cases, wire by constructor; keep @ContractAction names/signatures."""
from pathlib import Path
R=Path('contract/src/main/java/com/ororura')
contract=R/'api/PostContract.java'
api=R/'api/IPostContract.java'
assert (R/'infrastructure/persistence/ContractStateUserRepository.java').exists(), 'Run 02 first'
assert 'void creditUser(' in contract.read_text() and 'void creditUser(' in api.read_text(), (
    'Stage 03 requires the corrected contract from fix_waves_postoffice.py (run it BEFORE stage 01). '
    'Do not apply the old patch to moved files. Roll back to the clean branch if needed.'
)
assert 'private final Mapping<String> ownerMapping' in contract.read_text(), 'Expected corrected owner validation before extraction'
files={
'application/security/AccessPolicy.java': '''package com.ororura.application.security;
import com.ororura.application.context.ContractContext;
import com.ororura.domain.model.User;
import com.ororura.domain.model.UserRole;
import com.ororura.domain.repository.ContractMetadataRepository;
public final class AccessPolicy {
    private final ContractMetadataRepository metadata;
    private final ContractContext context;
    public AccessPolicy(ContractMetadataRepository metadata, ContractContext context) {
        this.metadata = metadata; this.context = context;
    }
    public void requireOwner() {
        String owner = metadata.findOwner().orElseThrow(() ->
            new IllegalStateException("Владелец контракта не задан"));
        if (!owner.equals(context.caller())) {
            throw new SecurityException("Только администратор может выполнить операцию");
        }
    }
    public void requireEmployee(User user) {
        if (!UserRole.EMPLOYEE.equals(user.getRole()) || user.getPostId() == null) {
            throw new SecurityException("Действие доступно сотруднику отделения");
        }
    }
}
''',
'application/service/UserService.java': '''package com.ororura.application.service;
import com.ororura.application.context.ContractContext;
import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.User;
import com.ororura.domain.model.UserRole;
import com.ororura.domain.repository.UserRepository;
public final class UserService {
    private final UserRepository users;
    private final PostOfficeService offices;
    private final ContractContext context;
    private final AccessPolicy access;
    public UserService(UserRepository users, PostOfficeService offices,
                       ContractContext context, AccessPolicy access) {
        this.users = users; this.offices = offices; this.context = context; this.access = access;
    }
    public User requireUser(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Некорректный адрес пользователя");
        }
        return users.findByAddress(address).orElseThrow(() ->
            new IllegalStateException("Пользователь не найден"));
    }
    public void createUser(User request) {
        String caller = context.caller();
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Укажите имя пользователя");
        }
        if (users.findByAddress(caller).isPresent()) {
            throw new IllegalStateException("Пользователь уже зарегистрирован");
        }
        User user = new User();
        user.setBlockchainAddress(caller);
        user.setName(request.getName());
        user.setHomeAddress(request.getHomeAddress());
        user.setBalance(0);
        user.setRole(UserRole.USER);
        users.save(user);
    }
    public void changePersonalData(User request) {
        User user = requireUser(context.caller());
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Укажите имя пользователя");
        }
        user.setName(request.getName());
        user.setHomeAddress(request.getHomeAddress());
        users.save(user);
    }
    public void setPostmanEmployee(String employee, int officeId, boolean enabled) {
        access.requireOwner();
        User user = requireUser(employee);
        if (enabled) {
            offices.requireOffice(officeId);
            user.setRole(UserRole.EMPLOYEE);
            user.setPostId(String.valueOf(officeId));
        } else {
            user.setRole(UserRole.USER);
            user.setPostId(null);
        }
        users.save(user);
    }
    public void creditUser(String address, double amount) {
        access.requireOwner();
        TransferService.requirePositiveAmount(amount);
        User user = requireUser(address);
        double result = user.getBalance() + amount;
        if (!Double.isFinite(result)) throw new IllegalStateException("Переполнение баланса");
        user.setBalance(result);
        users.save(user);
    }
}
''',
'application/service/TransferService.java': '''package com.ororura.application.service;
import com.ororura.application.context.ContractContext;
import com.ororura.domain.model.MoneyTransfer;
import com.ororura.domain.model.User;
import com.ororura.domain.repository.TransferRepository;
import com.ororura.domain.repository.UserRepository;
import java.util.List;
public final class TransferService {
    private final TransferRepository transfers;
    private final UserRepository users;
    private final UserService userService;
    private final ContractContext context;
    public TransferService(TransferRepository transfers, UserRepository users,
                           UserService userService, ContractContext context) {
        this.transfers=transfers; this.users=users; this.userService=userService; this.context=context;
    }
    public static void requirePositiveAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной и конечной");
        }
    }
    private MoneyTransfer byIndex(List<MoneyTransfer> all, int id) {
        if (id < 0 || id >= all.size()) throw new IllegalArgumentException("Перевод не найден: " + id);
        return all.get(id);
    }
    public void transferMoney(MoneyTransfer request) {
        String caller=context.caller();
        User sender=userService.requireUser(caller);
        if (request == null || request.getTo() == null || request.getTo().isBlank()
                || caller.equals(request.getTo())) {
            throw new IllegalArgumentException("Некорректный получатель");
        }
        userService.requireUser(request.getTo());
        requirePositiveAmount(request.getAmount());
        if (sender.getBalance() < request.getAmount()) {
            throw new IllegalStateException("Недостаточно средств");
        }
        MoneyTransfer transfer = new MoneyTransfer();
        transfer.setFrom(caller);
        transfer.setTo(request.getTo());
        transfer.setAmount(request.getAmount());
        transfer.setLifeTime(request.getLifeTime());
        transfer.setActive(true);
        List<MoneyTransfer> all=transfers.findAll();
        all.add(transfer);
        transfers.saveAll(all);
    }
    public void acceptTransfer(int id) {
        List<MoneyTransfer> all=transfers.findAll();
        MoneyTransfer transfer=byIndex(all,id);
        String caller=context.caller();
        if (!caller.equals(transfer.getTo())) throw new SecurityException("Нельзя принять чужой перевод");
        if (!transfer.isActive()) throw new IllegalStateException("Перевод уже обработан");
        requirePositiveAmount(transfer.getAmount());
        User sender=userService.requireUser(transfer.getFrom());
        User recipient=userService.requireUser(caller);
        if (sender.getBalance() < transfer.getAmount()) {
            throw new IllegalStateException("Недостаточно средств у отправителя");
        }
        double recipientBalance=recipient.getBalance()+transfer.getAmount();
        if (!Double.isFinite(recipientBalance)) throw new IllegalStateException("Переполнение баланса");
        sender.setBalance(sender.getBalance()-transfer.getAmount());
        recipient.setBalance(recipientBalance);
        transfer.setActive(false);
        users.save(sender);
        users.save(recipient);
        transfers.saveAll(all);
    }
    public void deniedTransfer(int id) {
        List<MoneyTransfer> all=transfers.findAll();
        MoneyTransfer transfer=byIndex(all,id);
        if (!context.caller().equals(transfer.getTo())) {
            throw new SecurityException("Нельзя отклонить чужой перевод");
        }
        if (!transfer.isActive()) throw new IllegalStateException("Перевод уже обработан");
        transfer.setActive(false);
        transfers.saveAll(all);
    }
}
''',
'application/service/PostOfficeService.java': '''package com.ororura.application.service;
import com.ororura.domain.model.PostOffice;
import com.ororura.domain.repository.PostOfficeRepository;
import java.util.HashMap;
public final class PostOfficeService {
    private final PostOfficeRepository repository;
    public PostOfficeService(PostOfficeRepository repository) { this.repository=repository; }
    public PostOffice requireOffice(int id) {
        PostOffice office=repository.findAll().get(id);
        if (office == null) throw new IllegalArgumentException("Отделение не найдено: " + id);
        return office;
    }
    public HashMap<Integer, PostOffice> all() { return repository.findAll(); }
    public void saveAll(HashMap<Integer, PostOffice> offices) { repository.saveAll(offices); }
}
''',
'application/service/ParcelService.java': '''package com.ororura.application.service;
import com.ororura.application.context.ContractContext;
import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.AcceptedParcel;
import com.ororura.domain.model.Parcel;
import com.ororura.domain.model.PostOffice;
import com.ororura.domain.model.User;
import com.ororura.domain.pricing.CalculateTotalCost;
import com.ororura.domain.repository.ParcelRepository;
import com.ororura.domain.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
public final class ParcelService {
    private final ParcelRepository parcels;
    private final UserRepository users;
    private final UserService userService;
    private final PostOfficeService offices;
    private final ContractContext context;
    private final AccessPolicy access;
    public ParcelService(ParcelRepository parcels, UserRepository users,
                         UserService userService, PostOfficeService offices,
                         ContractContext context, AccessPolicy access) {
        this.parcels=parcels; this.users=users; this.userService=userService;
        this.offices=offices; this.context=context; this.access=access;
    }
    public void sendPackage(Parcel parcel) {
        if (parcel == null || parcel.getTrackNumber() == null || parcel.getTrackNumber().isBlank()) {
            throw new IllegalArgumentException("Укажите трек-номер");
        }
        offices.requireOffice(parcel.getNextOffice());
        User sender=userService.requireUser(context.caller());
        List<Parcel> all=parcels.findAll();
        if (all.stream().anyMatch(p -> parcel.getTrackNumber().equals(p.getTrackNumber()))) {
            throw new IllegalStateException("Трек-номер уже существует");
        }
        double cost=CalculateTotalCost.calculateTotalCost(parcel);
        if (!Double.isFinite(cost) || cost <= 0) {
            throw new IllegalArgumentException("Некорректная стоимость доставки");
        }
        if (sender.getBalance() < cost) throw new IllegalStateException("Недостаточно средств");
        parcel.setFrom(context.caller());
        parcel.setShippingCost(cost);
        sender.setBalance(sender.getBalance()-cost);
        all.add(parcel);
        users.save(sender);
        parcels.saveAll(all);
    }
    public void checkoutParcel(int parcelId, int nextOfficeId) {
        User employee=userService.requireUser(context.caller());
        access.requireEmployee(employee);
        int currentOfficeId;
        try {
            currentOfficeId=Integer.parseInt(employee.getPostId().replaceFirst("^RR", ""));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Некорректное отделение сотрудника", e);
        }
        HashMap<Integer, PostOffice> allOffices=offices.all();
        PostOffice current=allOffices.get(currentOfficeId);
        if (current == null || !allOffices.containsKey(nextOfficeId)) {
            throw new IllegalArgumentException("Отделение не найдено");
        }
        List<Parcel> allParcels=parcels.findAll();
        if (parcelId < 0 || parcelId >= allParcels.size()) {
            throw new IllegalArgumentException("Посылка не найдена: " + parcelId);
        }
        Parcel parcel=allParcels.get(parcelId);
        parcel.setNextOffice(nextOfficeId);
        current.getAcceptedParcel().add(new AcceptedParcel(parcel, employee));
        parcels.saveAll(allParcels);
        offices.saveAll(allOffices);
    }
}
''',
'application/service/InitializationService.java': '''package com.ororura.application.service;
import com.ororura.application.context.ContractContext;
import com.ororura.domain.model.PostOffice;
import com.ororura.domain.repository.ContractMetadataRepository;
import com.ororura.domain.repository.ParcelRepository;
import com.ororura.domain.repository.TransferRepository;
import com.ororura.domain.repository.PostOfficeRepository;
import java.util.ArrayList;
import java.util.HashMap;
public final class InitializationService {
    private final ContractMetadataRepository metadata;
    private final ParcelRepository parcels;
    private final TransferRepository transfers;
    private final PostOfficeRepository offices;
    private final ContractContext context;
    public InitializationService(ContractMetadataRepository metadata, ParcelRepository parcels,
            TransferRepository transfers, PostOfficeRepository offices, ContractContext context) {
        this.metadata=metadata; this.parcels=parcels; this.transfers=transfers;
        this.offices=offices; this.context=context;
    }
    public void init() {
        if (metadata.findOwner().isPresent()) throw new IllegalStateException("Контракт уже инициализирован");
        HashMap<Integer, PostOffice> all = new HashMap<>();
        all.put(344000, new PostOffice(344000, "SORTING_CENTER"));
        all.put(347900, new PostOffice(347900, "MAIN_POST_OFFICE"));
        all.put(347901, new PostOffice(347901, "POST_OFFICE"));
        all.put(347902, new PostOffice(347902, "POST_OFFICE"));
        all.put(347903, new PostOffice(347903, "POST_OFFICE"));
        all.put(346770, new PostOffice(346770, "MAIN_POST_OFFICE"));
        all.put(346771, new PostOffice(346771, "POST_OFFICE"));
        metadata.saveOwner(context.caller());
        transfers.saveAll(new ArrayList<>());
        parcels.saveAll(new ArrayList<>());
        offices.saveAll(all);
    }
}
''',
'bootstrap/ContractFactory.java': '''package com.ororura.bootstrap;
import com.ororura.application.context.ContractContext;
import com.ororura.application.security.AccessPolicy;
import com.ororura.application.service.*;
import com.ororura.domain.repository.*;
import com.ororura.infrastructure.blockchain.WavesContractContext;
import com.ororura.infrastructure.persistence.*;
import com.wavesenterprise.sdk.contract.api.domain.ContractCall;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
public final class ContractFactory {
    private final UserService userService;
    private final TransferService transferService;
    private final ParcelService parcelService;
    private final PostOfficeService officeService;
    private final InitializationService initializationService;
    public ContractFactory(ContractState state, ContractCall call) {
        ContractContext context=new WavesContractContext(call);
        UserRepository users=new ContractStateUserRepository(state);
        TransferRepository transfers=new ContractStateTransferRepository(state);
        ParcelRepository parcels=new ContractStateParcelRepository(state);
        PostOfficeRepository offices=new ContractStatePostOfficeRepository(state);
        ContractMetadataRepository metadata=new ContractStateMetadataRepository(state);
        AccessPolicy access=new AccessPolicy(metadata, context);
        officeService=new PostOfficeService(offices);
        userService=new UserService(users, officeService, context, access);
        transferService=new TransferService(transfers, users, userService, context);
        parcelService=new ParcelService(parcels, users, userService, officeService, context, access);
        initializationService=new InitializationService(metadata, parcels, transfers, offices, context);
    }
    public UserService users() { return userService; }
    public TransferService transfers() { return transferService; }
    public ParcelService parcels() { return parcelService; }
    public InitializationService initialization() { return initializationService; }
}
''',
'api/PostContract.java': '''package com.ororura.api;
import com.ororura.application.service.*;
import com.ororura.bootstrap.ContractFactory;
import com.ororura.domain.model.MoneyTransfer;
import com.ororura.domain.model.Parcel;
import com.ororura.domain.model.User;
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler;
import com.wavesenterprise.sdk.contract.api.domain.ContractCall;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
@ContractHandler
public class PostContract implements IPostContract {
    private final InitializationService initialization;
    private final UserService users;
    private final TransferService transfers;
    private final ParcelService parcels;
    public PostContract(ContractState state, ContractCall call) {
        ContractFactory factory=new ContractFactory(state, call);
        this.initialization=factory.initialization();
        this.users=factory.users();
        this.transfers=factory.transfers();
        this.parcels=factory.parcels();
    }
    @Override public void init() { initialization.init(); }
    @Override public void createUser(User user) { users.createUser(user); }
    @Override public void changePersonalData(User user) { users.changePersonalData(user); }
    @Override public void setPostmanEmployee(String employee, int officeId, boolean enabled) {
        users.setPostmanEmployee(employee, officeId, enabled);
    }
    @Override public void creditUser(String address, double amount) { users.creditUser(address, amount); }
    @Override public void transferMoney(MoneyTransfer transfer) { transfers.transferMoney(transfer); }
    @Override public void acceptTransfer(int id) { transfers.acceptTransfer(id); }
    @Override public void deniedTransfer(int id) { transfers.deniedTransfer(id); }
    @Override public void sendPackage(Parcel parcel) { parcels.sendPackage(parcel); }
    @Override public void checkoutParcel(int parcelId, int nextPostId) {
        parcels.checkoutParcel(parcelId, nextPostId);
    }
}
''',
}
# Save a copy of the corrected contract outside the source tree before replacing it.
backup=Path('.migration-backups/PostContract.before-layering.java.txt')
backup.parent.mkdir(parents=True, exist_ok=True)
if backup.exists(): raise SystemExit('Backup already exists; check migration state')
backup.write_text(contract.read_text())
for name,s in files.items():
    p=R/name; p.parent.mkdir(parents=True, exist_ok=True);p.write_text(s)
metadata=R/'infrastructure/persistence/ContractStateMetadataRepository.java'
s=metadata.read_text()
s=s.replace('    private final Mapping<String> mapping;', '    private final Mapping<String> mapping;\n    private final ContractState state;')
s=s.replace('    public ContractStateMetadataRepository(ContractState state) {\n', '    public ContractStateMetadataRepository(ContractState state) {\n        this.state = state;\n')
s=s.replace('    @Override public void saveOwner(String address) { mapping.put("OWNER", address); }',
'''    @Override public void saveOwner(String address) {
        mapping.put("OWNER", address);
        state.put("CONTRACT_CALL", address); // legacy key retained
    }''')
metadata.write_text(s)
gitignore=Path('.gitignore')
gitignore.write_text(gitignore.read_text() + '\n# Local migration backup\n.migration-backups/\n')
print('Stage 3 done. Local backup: .migration-backups/PostContract.before-layering.java.txt')
