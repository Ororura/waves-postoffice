package com.ororura.api;

import com.ororura.api.dto.*;
import com.wavesenterprise.sdk.contract.api.annotation.ContractAction;
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit;
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam;

/** V2: not compatible with V1 call payloads or previously persisted state. */
public interface IPostContract {
  @ContractInit
  void init();

  @ContractAction
  void createUser(@InvokeParam(name = "user") RegisterUserRequest request);

  @ContractAction
  void changePersonalData(@InvokeParam(name = "user") UpdateUserRequest request);

  @ContractAction
  void creditUser(
      @InvokeParam(name = "user") String address, @InvokeParam(name = "amount") long amount);

  @ContractAction
  void transferMoney(@InvokeParam(name = "money") CreateTransferRequest request);

  @ContractAction
  void acceptTransfer(@InvokeParam(name = "id") int id);

  @ContractAction
  void deniedTransfer(@InvokeParam(name = "id") int id);

  @ContractAction
  void setPostmanEmployee(
      @InvokeParam(name = "user") String employee,
      @InvokeParam(name = "postOfficeId") int officeId,
      @InvokeParam(name = "status") boolean enabled);

  @ContractAction
  void sendPackage(@InvokeParam(name = "package") CreateParcelRequest request);

  @ContractAction
  void checkoutParcel(
      @InvokeParam(name = "trackingNumber") String trackingNumber,
      @InvokeParam(name = "nextPostId") int nextPostId);
}
