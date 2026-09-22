package com.ororura.application.service;

import com.ororura.application.context.ContractContext;
import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.User;
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
    if (request == null) {
      throw new IllegalArgumentException("Не указан пользователь");
    }
    if (users.findByAddress(caller).isPresent()) {
      throw new IllegalStateException("Пользователь уже зарегистрирован");
    }
    users.save(User.register(caller, request.getName(), request.getHomeAddress()));
  }

  public void changePersonalData(User request) {
    if (request == null) {
      throw new IllegalArgumentException("Не указан пользователь");
    }
    User user = requireUser(context.caller());
    user.changeProfile(request.getName(), request.getHomeAddress());
    users.save(user);
  }

  public void setPostmanEmployee(String employee, int officeId, boolean enabled) {
    access.requireOwner();
    User user = requireUser(employee);
    if (enabled) {
      offices.requireOffice(officeId);
      user.assignToOffice(officeId);
    } else {
      user.removeFromOffice();
    }
    users.save(user);
  }

  public void creditUser(String address, long amount) {
    access.requireOwner();
    User user = requireUser(address);
    user.credit(amount);
    users.save(user);
  }
}
