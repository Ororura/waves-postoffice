package com.ororura.api;

import com.ororura.api.dto.*;
import com.ororura.application.service.*;
import com.ororura.bootstrap.ContractFactory;
import com.ororura.domain.model.MoneyTransfer;
import com.ororura.domain.model.Parcel;
import com.ororura.domain.model.User;
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler;
import com.wavesenterprise.sdk.contract.api.domain.ContractCall;
import com.wavesenterprise.sdk.contract.api.state.ContractState;

@ContractHandler
public class WavesPostOfficeContract implements PostOfficeContract {
  private final InitializationService initialization;
  private final UserService users;
  private final TransferService transfers;
  private final ParcelService parcels;

  public WavesPostOfficeContract(ContractState state, ContractCall call) {
    ContractFactory factory = new ContractFactory(state, call);
    initialization = factory.initialization();
    users = factory.users();
    transfers = factory.transfers();
    parcels = factory.parcels();
  }

  @Override
  public void init() {
    initialization.init();
  }

  @Override
  public void createUser(RegisterUserRequest request) {
    if (request == null) throw new IllegalArgumentException("Не указан пользователь");
    User user = new User();
    user.setName(request.getName());
    user.setHomeAddress(request.getHomeAddress());
    users.createUser(user);
  }

  @Override
  public void changePersonalData(UpdateUserRequest request) {
    if (request == null) throw new IllegalArgumentException("Не указан пользователь");
    User user = new User();
    user.setName(request.getName());
    user.setHomeAddress(request.getHomeAddress());
    users.changePersonalData(user);
  }

  @Override
  public void creditUser(String address, long amount) {
    users.creditUser(address, amount);
  }

  @Override
  public void setPostmanEmployee(String employee, int officeId, boolean enabled) {
    users.setPostmanEmployee(employee, officeId, enabled);
  }

  @Override
  public void transferMoney(CreateTransferRequest request) {
    if (request == null) throw new IllegalArgumentException("Не указан перевод");
    MoneyTransfer transfer = new MoneyTransfer();
    transfer.setTo(request.getTo());
    transfer.setAmount(request.getAmount());
    // No chain-time source in ContractContext; expiry is deliberately not exposed in V2 API.
    transfers.transferMoney(transfer);
  }

  @Override
  public void acceptTransfer(int id) {
    transfers.acceptTransfer(id);
  }

  @Override
  public void deniedTransfer(int id) {
    transfers.deniedTransfer(id);
  }

  @Override
  public void sendPackage(CreateParcelRequest request) {
    if (request == null) throw new IllegalArgumentException("Не указана посылка");
    Parcel parcel = new Parcel();
    parcel.setTrackNumber(request.getTrackNumber());
    parcel.setTo(request.getTo());
    parcel.setType(request.getType());
    parcel.setShippingClass(request.getShippingClass());
    parcel.setDeliveryTime(request.getDeliveryTime());
    parcel.setWeight(request.getWeight());
    parcel.setDeclaredValue(request.getDeclaredValue());
    parcel.setTotalValue(request.getTotalValue());
    parcel.setAddressTo(request.getAddressTo());
    parcel.setAddressFrom(request.getAddressFrom());
    parcel.setNextOffice(request.getNextOffice());
    parcels.sendPackage(parcel);
  }

  @Override
  public void checkoutParcel(String trackingNumber, int nextPostId) {
    parcels.checkoutParcel(trackingNumber, nextPostId);
  }
}
