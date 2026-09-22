package com.ororura.application.service;

import com.ororura.application.context.ContractContext;
import com.ororura.domain.model.MoneyTransfer;
import com.ororura.domain.model.User;
import com.ororura.domain.repository.TransferRepository;
import com.ororura.domain.repository.UserRepository;

public final class TransferService {
  private final TransferRepository transfers;
  private final UserRepository users;
  private final UserService userService;
  private final ContractContext context;

  public TransferService(
      TransferRepository transfers,
      UserRepository users,
      UserService userService,
      ContractContext context) {
    this.transfers = transfers;
    this.users = users;
    this.userService = userService;
    this.context = context;
  }

  public int transferMoney(MoneyTransfer request) {
    if (request == null) throw new IllegalArgumentException("Не указан перевод");
    String senderAddress = context.caller();
    User sender = userService.requireUser(senderAddress);
    userService.requireUser(request.getTo());
    MoneyTransfer transfer =
        MoneyTransfer.create(
            senderAddress, request.getTo(), request.getAmount(), request.getLifeTime());
    if (sender.getBalance() < transfer.getAmount())
      throw new IllegalStateException("Недостаточно средств");
    return transfers.create(transfer);
  }

  public void acceptTransfer(int id) {
    MoneyTransfer transfer = transfers.requireById(id);
    transfer.requireRecipient(context.caller());
    transfer.requireActive();
    User.requirePositive(transfer.getAmount());
    User sender = userService.requireUser(transfer.getFrom());
    User recipient = userService.requireUser(context.caller());
    Math.addExact(recipient.getBalance(), transfer.getAmount());
    sender.debit(transfer.getAmount());
    recipient.credit(transfer.getAmount());
    transfer.accept();
    users.save(sender);
    users.save(recipient);
    transfers.save(id, transfer);
  }

  public void deniedTransfer(int id) {
    MoneyTransfer transfer = transfers.requireById(id);
    transfer.requireRecipient(context.caller());
    transfer.reject();
    transfers.save(id, transfer);
  }
}
