package com.ororura.application.service;

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

  private MoneyTransfer requireTransferByIndex(List<MoneyTransfer> all, int id) {
    if (id < 0 || id >= all.size()) {
      throw new IllegalArgumentException("Перевод не найден: " + id);
    }
    return all.get(id);
  }

  public void transferMoney(MoneyTransfer request) {
    if (request == null) {
      throw new IllegalArgumentException("Не указан перевод");
    }
    String caller = context.caller();
    User sender = userService.requireUser(caller);
    if (request.getTo() == null || request.getTo().isBlank() || caller.equals(request.getTo())) {
      throw new IllegalArgumentException("Некорректный получатель");
    }
    userService.requireUser(request.getTo());
    MoneyTransfer transfer =
        MoneyTransfer.create(caller, request.getTo(), request.getAmount(), request.getLifeTime());
    if (sender.getBalance() < transfer.getAmount()) {
      throw new IllegalStateException("Недостаточно средств");
    }
    List<MoneyTransfer> all = transfers.findAll();
    all.add(transfer);
    transfers.saveAll(all);
  }

  public void acceptTransfer(int id) {
    List<MoneyTransfer> all = transfers.findAll();
    MoneyTransfer transfer = requireTransferByIndex(all, id);
    transfer.requireRecipient(context.caller());
    transfer.requireActive();
    User.requirePositive(transfer.getAmount());

    User sender = userService.requireUser(transfer.getFrom());
    User recipient = userService.requireUser(context.caller());
    // Validate both balances before changing either object.
    Math.addExact(recipient.getBalance(), transfer.getAmount());
    sender.debit(transfer.getAmount());
    recipient.credit(transfer.getAmount());
    transfer.accept();

    users.save(sender);
    users.save(recipient);
    transfers.saveAll(all);
  }

  public void deniedTransfer(int id) {
    List<MoneyTransfer> all = transfers.findAll();
    MoneyTransfer transfer = requireTransferByIndex(all, id);
    transfer.requireRecipient(context.caller());
    transfer.reject();
    transfers.saveAll(all);
  }
}
