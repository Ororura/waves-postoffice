package com.ororura.api;

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
    ContractFactory factory = new ContractFactory(state, call);
    this.initialization = factory.initialization();
    this.users = factory.users();
    this.transfers = factory.transfers();
    this.parcels = factory.parcels();
  }

  @Override
  public void init() {
    initialization.init();
  }

  @Override
  public void createUser(User user) {
    users.createUser(user);
  }

  @Override
  public void changePersonalData(User user) {
    users.changePersonalData(user);
  }

  @Override
  public void setPostmanEmployee(String employee, int officeId, boolean enabled) {
    users.setPostmanEmployee(employee, officeId, enabled);
  }

  @Override
  public void creditUser(String address, long amount) {
    users.creditUser(address, amount);
  }

  @Override
  public void transferMoney(MoneyTransfer transfer) {
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
  public void sendPackage(Parcel parcel) {
    parcels.sendPackage(parcel);
  }

  @Override
  public void checkoutParcel(int parcelId, int nextPostId) {
    parcels.checkoutParcel(parcelId, nextPostId);
  }
}
