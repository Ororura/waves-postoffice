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
    String caller = context.caller();
    User sender = userService.requireUser(caller);
    if (request == null
        || request.getTo() == null
        || request.getTo().isBlank()
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
    List<MoneyTransfer> all = transfers.findAll();
    all.add(transfer);
    transfers.saveAll(all);
  }

  public void acceptTransfer(int id) {
    List<MoneyTransfer> all = transfers.findAll();
    MoneyTransfer transfer = byIndex(all, id);
    String caller = context.caller();
    if (!caller.equals(transfer.getTo()))
      throw new SecurityException("Нельзя принять чужой перевод");
    if (!transfer.isActive()) throw new IllegalStateException("Перевод уже обработан");
    requirePositiveAmount(transfer.getAmount());
    User sender = userService.requireUser(transfer.getFrom());
    User recipient = userService.requireUser(caller);
    if (sender.getBalance() < transfer.getAmount()) {
      throw new IllegalStateException("Недостаточно средств у отправителя");
    }
    double recipientBalance = recipient.getBalance() + transfer.getAmount();
    if (!Double.isFinite(recipientBalance)) throw new IllegalStateException("Переполнение баланса");
    sender.setBalance(sender.getBalance() - transfer.getAmount());
    recipient.setBalance(recipientBalance);
    transfer.setActive(false);
    users.save(sender);
    users.save(recipient);
    transfers.saveAll(all);
  }

  public void deniedTransfer(int id) {
    List<MoneyTransfer> all = transfers.findAll();
    MoneyTransfer transfer = byIndex(all, id);
    if (!context.caller().equals(transfer.getTo())) {
      throw new SecurityException("Нельзя отклонить чужой перевод");
    }
    if (!transfer.isActive()) throw new IllegalStateException("Перевод уже обработан");
    transfer.setActive(false);
    transfers.saveAll(all);
  }
}
