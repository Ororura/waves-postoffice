package com.ororura.app;

import com.ororura.api.IPostContract;
import com.ororura.model.MoneyTransfer;
import com.ororura.model.Parcel;
import com.ororura.model.User;
import com.ororura.utils.CalculateTotalCost;
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler;
import com.wavesenterprise.sdk.contract.api.domain.ContractCall;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ororura.api.IPostContract.Keys.*;

@ContractHandler
public class PostContract implements IPostContract {

    private final ContractState contractState;
    private final ContractCall contractCall;
    private final Mapping<User> userMapping;
    private final Mapping<List<MoneyTransfer>> transferMoneyMapping;
    private final Mapping<List<Parcel>> parcelMapping;
    private final List<MoneyTransfer> monetTransferList = new ArrayList<>();
    private final List<Parcel> parcelList = new ArrayList<>();
    private final String owner;


    public PostContract(ContractState contractState, ContractCall contractCall) {
        this.contractState = contractState;
        this.contractCall = contractCall;
        this.userMapping = this.contractState.getMapping(new TypeReference<>() {
        }, USERS_MAPPING);
        this.transferMoneyMapping = this.contractState.getMapping(new TypeReference<>() {
        }, TRANSFER_MONEY_MAPPING);
        this.parcelMapping = this.contractState.getMapping(new TypeReference<List<Parcel>>() {
        }, PARCEL_MAPPING);
        this.owner = contractCall.getCaller();
    }

    @Override
    public void init() {
        this.contractState.put("CONTRACT_CALL", contractCall.getCaller());
        this.transferMoneyMapping.put("_", this.monetTransferList);
        this.parcelMapping.put("_", this.parcelList);
    }

    @Override
    public void sendPackage(Parcel parcel) {
        final double totalcost = CalculateTotalCost.calculateTotalCost(parcel);
        Optional<User> userSender = this.userMapping.tryGet(this.contractCall.getCaller());
        Optional<List<Parcel>> parcelList = this.parcelMapping.tryGet("_");

        if (parcelList.isEmpty()) {
            throw new IllegalStateException("Посылки не найдены!");
        }

        if (userSender.isEmpty()) {
            throw new IllegalStateException("Пользователь не найден!");
        }

        if (totalcost > userSender.get().getBalance()) {
            throw new IllegalStateException("У вас недостаточно токенов!");
        }

        userSender.get().setBalance(userSender.get().getBalance() - totalcost);
        parcel.setShippingCost(totalcost);
        parcelList.get().add(parcel);

        this.userMapping.put(userSender.get().getBlockchainAddress(), userSender.get());
        this.parcelMapping.put("_", parcelList.get());
    }

    @Override
    public void setPostmanEmployee(String sender, boolean status) {
        if (!Objects.equals(this.owner, contractCall.getCaller())) {
            throw new IllegalStateException("Вы не администратор!");
        }
        ;
        Optional<User> user = this.userMapping.tryGet(sender);

        if (user.isEmpty()) {
            throw new IllegalStateException("Пользователь не найден!");
        }

        if (status) {
            user.get().setRole(Role.EMPLOYEE);
        } else {
            user.get().setRole(Role.USER);

        }

        this.userMapping.put(user.get().getBlockchainAddress(), user.get());
    }

    @Override
    public void transferMoney(MoneyTransfer moneyTransfer) {
        Optional<List<MoneyTransfer>> findedMoneyTransferMapping = this.transferMoneyMapping.tryGet("_");

        if (findedMoneyTransferMapping.isEmpty()) {
            throw new IllegalStateException("Список не найден!");
        }

        findedMoneyTransferMapping.get().add(moneyTransfer);
        this.transferMoneyMapping.put("_", findedMoneyTransferMapping.get());

    }

    @Override
    public void changePersonalData(User user) {
        Optional<User> foundedUser = this.userMapping.tryGet(this.contractCall.getCaller());

        if (foundedUser.isEmpty()) {
            throw new IllegalStateException("Пользователь не найден!");
        }

        if (Objects.equals(foundedUser.get().getBlockchainAddress(), this.contractCall.getCaller())) {
            throw new IllegalStateException("Вы не можете изменить чужие данные!");
        }

        this.userMapping.put(user.getBlockchainAddress(), user);
    }

    @Override
    public void acceptTransfer(int id) {
        Optional<List<MoneyTransfer>> findedMoneyTransferMapping = this.transferMoneyMapping.tryGet("_");
        Optional<User> acceptUser = this.userMapping.tryGet(this.contractCall.getCaller());

        if (acceptUser.isEmpty()) {
            throw new IllegalStateException("пользователь не найден!");
        }


        if (findedMoneyTransferMapping.isEmpty()) {
            throw new IllegalStateException("Список не найден!");
        }

        if (!Objects.equals(findedMoneyTransferMapping.get().get(id).getTo(), this.contractCall.getCaller())) {
            throw new IllegalStateException("Вы не можете принять чужой перевод!");
        }

        Optional<User> senderUser = this.userMapping.tryGet(findedMoneyTransferMapping.get().get(id).getFrom());

        if (senderUser.isEmpty()) {
            throw new IllegalStateException("пользователь не найден!");
        }

        if (!findedMoneyTransferMapping.get().get(id).isActive()) {
            throw new IllegalStateException("Перевод не активен!");
        }

        acceptUser.get().setBalance(acceptUser.get().getBalance() + findedMoneyTransferMapping.get().get(id).getAmount());
        senderUser.get().setBalance(senderUser.get().getBalance() - findedMoneyTransferMapping.get().get(id).getAmount());
        findedMoneyTransferMapping.get().get(id).setActive(false);
        this.userMapping.put(acceptUser.get().getBlockchainAddress(), acceptUser.get());
        this.userMapping.put(acceptUser.get().getBlockchainAddress(), acceptUser.get());
        this.transferMoneyMapping.put("", findedMoneyTransferMapping.get());
    }

    @Override
    public void deniedTransfer(int id) {
        Optional<List<MoneyTransfer>> findedMoneyTransferMapping = this.transferMoneyMapping.tryGet("_");
        Optional<User> acceptUser = this.userMapping.tryGet(this.contractCall.getCaller());

        if (acceptUser.isEmpty()) {
            throw new IllegalStateException("Пользователь не найден!");
        }


        if (findedMoneyTransferMapping.isEmpty()) {
            throw new IllegalStateException("Список не найден!");
        }

        if (!Objects.equals(findedMoneyTransferMapping.get().get(id).getTo(), this.contractCall.getCaller())) {
            throw new IllegalStateException("Вы не можете принять чужой перевод!");
        }

        Optional<User> senderUser = this.userMapping.tryGet(findedMoneyTransferMapping.get().get(id).getFrom());

        if (senderUser.isEmpty()) {
            throw new IllegalStateException("Пользователь не найден!");
        }

        if (Objects.equals(senderUser.get().getBlockchainAddress(), findedMoneyTransferMapping.get().get(id).getFrom()) || Objects.equals(acceptUser.get().getBlockchainAddress(), findedMoneyTransferMapping.get().get(id).getTo())) {
            findedMoneyTransferMapping.get().get(id).setActive(false);
            this.transferMoneyMapping.put("", findedMoneyTransferMapping.get());
            return;
        }

        throw new IllegalStateException("Вы не можете отменить чужой перевод!");
    }

    @Override
    public void createUser(User user) {
        Optional<User> newUser = this.userMapping.tryGet(user.getBlockchainAddress());
        if (newUser.isPresent()) {
            throw new IllegalStateException("Пользователь с таким адресом уже есть в системе!");
        }

        this.userMapping.put(user.getBlockchainAddress(), user);
    }
}
