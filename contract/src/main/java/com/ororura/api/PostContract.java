package com.ororura.api;

import com.ororura.api.IPostContract;
import com.ororura.domain.model.*;
import com.ororura.domain.pricing.CalculateTotalCost;
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler;
import com.wavesenterprise.sdk.contract.api.domain.ContractCall;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;

import java.util.*;

import static com.ororura.api.IPostContract.Keys.*;
import static com.ororura.api.IPostContract.PostOfficeTypes.*;

@ContractHandler
public class PostContract implements IPostContract {

    private final ContractState contractState;
    private final ContractCall contractCall;
    private final Mapping<User> userMapping;
    private final Mapping<List<MoneyTransfer>> transferMoneyMapping;
    private final Mapping<List<Parcel>> parcelMapping;
    private final Mapping<HashMap<Integer, PostOffice>> postOfficeMapping;
    private final List<MoneyTransfer> monetTransferList = new ArrayList<>();
    private final List<Parcel> parcelList = new ArrayList<>();
    private final HashMap<Integer, PostOffice> postOfficeHashMap = new HashMap<>();
    private final Mapping<String> ownerMapping;


    public PostContract(ContractState contractState, ContractCall contractCall) {
        this.contractState = contractState;
        this.contractCall = contractCall;
        this.userMapping = this.contractState.getMapping(new TypeReference<>() {
        }, USERS_MAPPING);
        this.transferMoneyMapping = this.contractState.getMapping(new TypeReference<>() {
        }, TRANSFER_MONEY_MAPPING);
        this.parcelMapping = this.contractState.getMapping(new TypeReference<List<Parcel>>() {
        }, PARCEL_MAPPING);
        this.ownerMapping = contractState.getMapping(
                new TypeReference<String>() {}, "CONTRACT_META");
        this.postOfficeMapping = this.contractState.getMapping(new TypeReference<>() {
        }, OFFICE_MAPPING);
    }

    @Override
    public void init() {
        if (ownerMapping.tryGet("OWNER").isPresent()) {
            throw new IllegalStateException("Контракт уже инициализирован");
        }
        ownerMapping.put("OWNER", contractCall.getCaller());
        contractState.put("CONTRACT_CALL", contractCall.getCaller());
        transferMoneyMapping.put("_", new ArrayList<>());
        parcelMapping.put("_", new ArrayList<>());

        HashMap<Integer, PostOffice> offices = new HashMap<>();
        offices.put(344000, new PostOffice(344000, SORTING_CENTER));
        offices.put(347900, new PostOffice(347900, MAIN_POST_OFFICE));
        offices.put(347901, new PostOffice(347901, POST_OFFICE));
        offices.put(347902, new PostOffice(347902, POST_OFFICE));
        offices.put(347903, new PostOffice(347903, POST_OFFICE));
        offices.put(346770, new PostOffice(346770, MAIN_POST_OFFICE));
        offices.put(346771, new PostOffice(346771, POST_OFFICE));
        postOfficeMapping.put("_", offices);
    }

    @Override
    public void checkoutParcel(int parcelId, int nextPostId) {
        User employee = requireUser(contractCall.getCaller());
        if (!Role.EMPLOYEE.equals(employee.getRole()) || employee.getPostId() == null) {
            throw new SecurityException("Действие доступно сотруднику отделения");
        }
        int currentOfficeId;
        try {
            currentOfficeId = Integer.parseInt(employee.getPostId().replaceFirst("^RR", ""));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Некорректное отделение сотрудника", e);
        }
        HashMap<Integer, PostOffice> offices = requireOffices();
        PostOffice current = offices.get(currentOfficeId);
        if (current == null || !offices.containsKey(nextPostId)) {
            throw new IllegalArgumentException("Отделение не найдено");
        }
        List<Parcel> parcels = requireParcels();
        Parcel parcel = getByIndex(parcels, parcelId, "Посылка");
        // Полноценная проверка маршрута потребует отдельного currentOffice/status в Parcel.
        parcel.setNextOffice(nextPostId);
        current.getAcceptedParcel().add(new AcceptedParcel(parcel, employee));
        parcelMapping.put("_", parcels);
        postOfficeMapping.put("_", offices);
    }


    @Override
    public void sendPackage(Parcel parcel) {
        if (parcel == null || parcel.getTrackNumber() == null || parcel.getTrackNumber().isBlank()) {
            throw new IllegalArgumentException("Укажите трек-номер");
        }
        requireOffice(parcel.getNextOffice());
        User sender = requireUser(contractCall.getCaller());
        List<Parcel> parcels = requireParcels();
        if (parcels.stream().anyMatch(existing ->
                parcel.getTrackNumber().equals(existing.getTrackNumber()))) {
            throw new IllegalStateException("Трек-номер уже существует");
        }
        double totalCost = CalculateTotalCost.calculateTotalCost(parcel);
        if (!Double.isFinite(totalCost) || totalCost <= 0) {
            throw new IllegalArgumentException("Некорректная стоимость доставки");
        }
        if (sender.getBalance() < totalCost) {
            throw new IllegalStateException("Недостаточно средств");
        }
        parcel.setFrom(contractCall.getCaller());
        parcel.setShippingCost(totalCost);
        sender.setBalance(sender.getBalance() - totalCost);
        parcels.add(parcel);
        userMapping.put(sender.getBlockchainAddress(), sender);
        parcelMapping.put("_", parcels);
    }

    @Override
    public void setPostmanEmployee(String employee, int postOfficeId, boolean status) {
        requireOwner();
        User user = requireUser(employee);
        if (status) {
            requireOffice(postOfficeId);
            user.setRole(Role.EMPLOYEE);
            user.setPostId(String.valueOf(postOfficeId));
        } else {
            user.setRole(Role.USER);
            user.setPostId(null);
        }
        userMapping.put(employee, user);
    }

    @Override
    public void transferMoney(MoneyTransfer request) {
        String caller = contractCall.getCaller();
        User sender = requireUser(caller);
        if (request == null || request.getTo() == null || request.getTo().isBlank()
                || caller.equals(request.getTo())) {
            throw new IllegalArgumentException("Некорректный получатель");
        }
        requireUser(request.getTo());
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
        List<MoneyTransfer> transfers = requireTransfers();
        transfers.add(transfer);
        transferMoneyMapping.put("_", transfers);
    }

    @Override
    public void changePersonalData(User request) {
        User user = requireUser(contractCall.getCaller());
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Укажите имя пользователя");
        }
        user.setName(request.getName());
        user.setHomeAddress(request.getHomeAddress());
        // Адрес, роль, отделение и баланс клиент менять не может.
        userMapping.put(contractCall.getCaller(), user);
    }

    @Override
    public void acceptTransfer(int id) {
        List<MoneyTransfer> transfers = requireTransfers();
        MoneyTransfer transfer = getByIndex(transfers, id, "Перевод");
        String caller = contractCall.getCaller();
        if (!caller.equals(transfer.getTo())) {
            throw new SecurityException("Нельзя принять чужой перевод");
        }
        if (!transfer.isActive()) {
            throw new IllegalStateException("Перевод уже обработан");
        }
        requirePositiveAmount(transfer.getAmount());
        User sender = requireUser(transfer.getFrom());
        User recipient = requireUser(caller);
        if (sender.getBalance() < transfer.getAmount()) {
            throw new IllegalStateException("Недостаточно средств у отправителя");
        }
        double senderBalance = sender.getBalance() - transfer.getAmount();
        double recipientBalance = recipient.getBalance() + transfer.getAmount();
        if (!Double.isFinite(recipientBalance)) {
            throw new IllegalStateException("Переполнение баланса");
        }
        sender.setBalance(senderBalance);
        recipient.setBalance(recipientBalance);
        transfer.setActive(false);
        userMapping.put(sender.getBlockchainAddress(), sender);
        userMapping.put(recipient.getBlockchainAddress(), recipient);
        transferMoneyMapping.put("_", transfers);
    }

    @Override
    public void deniedTransfer(int id) {
        List<MoneyTransfer> transfers = requireTransfers();
        MoneyTransfer transfer = getByIndex(transfers, id, "Перевод");
        if (!contractCall.getCaller().equals(transfer.getTo())) {
            throw new SecurityException("Нельзя отклонить чужой перевод");
        }
        if (!transfer.isActive()) {
            throw new IllegalStateException("Перевод уже обработан");
        }
        transfer.setActive(false);
        transferMoneyMapping.put("_", transfers);
    }

    @Override
    public void createUser(User request) {
        String caller = contractCall.getCaller();
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Укажите имя пользователя");
        }
        if (userMapping.tryGet(caller).isPresent()) {
            throw new IllegalStateException("Пользователь уже зарегистрирован");
        }
        User user = new User();
        user.setBlockchainAddress(caller);
        user.setName(request.getName());
        user.setHomeAddress(request.getHomeAddress());
        user.setBalance(0);
        user.setRole(Role.USER);
        userMapping.put(caller, user);
    }

    @Override
    public void creditUser(String address, double amount) {
        requireOwner();
        requirePositiveAmount(amount);
        User user = requireUser(address);
        double newBalance = user.getBalance() + amount;
        if (!Double.isFinite(newBalance)) {
            throw new IllegalStateException("Переполнение баланса");
        }
        user.setBalance(newBalance);
        userMapping.put(address, user);
    }


    private void requireOwner() {
        String owner = ownerMapping.tryGet("OWNER")
                .orElseThrow(() -> new IllegalStateException("Владелец контракта не задан"));
        if (!owner.equals(contractCall.getCaller())) {
            throw new SecurityException("Только администратор может выполнить операцию");
        }
    }

    private User requireUser(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Некорректный адрес пользователя");
        }
        return userMapping.tryGet(address)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
    }

    private List<MoneyTransfer> requireTransfers() {
        return transferMoneyMapping.tryGet("_")
                .orElseThrow(() -> new IllegalStateException("Переводы не инициализированы"));
    }

    private List<Parcel> requireParcels() {
        return parcelMapping.tryGet("_")
                .orElseThrow(() -> new IllegalStateException("Посылки не инициализированы"));
    }

    private HashMap<Integer, PostOffice> requireOffices() {
        return postOfficeMapping.tryGet("_")
                .orElseThrow(() -> new IllegalStateException("Отделения не инициализированы"));
    }

    private PostOffice requireOffice(int officeId) {
        PostOffice office = requireOffices().get(officeId);
        if (office == null) {
            throw new IllegalArgumentException("Отделение не найдено: " + officeId);
        }
        return office;
    }

    private static void requirePositiveAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной и конечной");
        }
    }

    private static <T> T getByIndex(List<T> items, int index, String itemName) {
        if (index < 0 || index >= items.size()) {
            throw new IllegalArgumentException(itemName + " не найден(а): " + index);
        }
        return items.get(index);
    }

}
