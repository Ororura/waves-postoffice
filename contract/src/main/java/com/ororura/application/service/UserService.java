package com.ororura.application.service;

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

  public UserService(
      UserRepository users,
      PostOfficeService offices,
      ContractContext context,
      AccessPolicy access) {
    this.users = users;
    this.offices = offices;
    this.context = context;
    this.access = access;
  }

  public User requireUser(String address) {
    if (address == null || address.isBlank()) {
      throw new IllegalArgumentException("Некорректный адрес пользователя");
    }
    return users
        .findByAddress(address)
        .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
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
