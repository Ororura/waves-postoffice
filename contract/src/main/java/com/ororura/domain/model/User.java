package com.ororura.domain.model;

import static com.ororura.domain.model.UserRole.USER;

public class User {
  private String name;
  private String homeAddress;
  private String blockchainAddress;
  private double balance;
  private String role = USER;
  private String postId;

  public User(String name, String homeAddress, String blockchainAddress, int balance, String role) {
    this.name = name;
    this.homeAddress = homeAddress;
    this.blockchainAddress = blockchainAddress;
    this.balance = balance;
    this.role = role;
  }

  public User() {}

  public String getName() {
    return name;
  }

  public String getBlockchainAddress() {
    return blockchainAddress;
  }

  public void setBlockchainAddress(String blockchainAddress) {
    this.blockchainAddress = blockchainAddress;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHomeAddress() {
    return homeAddress;
  }

  public void setHomeAddress(String homeAddress) {
    this.homeAddress = homeAddress;
  }

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }

  public String getPostId() {
    return postId;
  }

  public void setPostId(String postId) {
    this.postId =
        postId == null || postId.isBlank()
            ? null
            : (postId.startsWith("RR") ? postId : "RR" + postId);
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  /** New registrations are always tied to the authenticated blockchain caller. */
  public static User register(String caller, String name, String homeAddress) {
    if (caller == null || caller.isBlank()) {
      throw new IllegalArgumentException("Не указан адрес пользователя");
    }
    User user = new User();
    user.setBlockchainAddress(caller);
    user.changeProfile(name, homeAddress);
    user.setBalance(0);
    user.setRole(UserRole.USER);
    return user;
  }

  public void changeProfile(String name, String homeAddress) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Укажите имя пользователя");
    }
    this.name = name;
    this.homeAddress = homeAddress;
  }

  public void debit(double amount) {
    requirePositiveFinite(amount);
    if (!Double.isFinite(balance) || balance < amount) {
      throw new IllegalStateException("Недостаточно средств");
    }
    double result = balance - amount;
    if (!Double.isFinite(result)) {
      throw new IllegalStateException("Некорректный баланс");
    }
    balance = result;
  }

  public void credit(double amount) {
    requirePositiveFinite(amount);
    if (!Double.isFinite(balance)) {
      throw new IllegalStateException("Некорректный баланс");
    }
    double result = balance + amount;
    if (!Double.isFinite(result)) {
      throw new IllegalStateException("Переполнение баланса");
    }
    balance = result;
  }

  public void assignToOffice(int officeId) {
    if (officeId <= 0) {
      throw new IllegalArgumentException("Некорректный идентификатор отделения");
    }
    setRole(UserRole.EMPLOYEE);
    setPostId(String.valueOf(officeId));
  }

  public void removeFromOffice() {
    setRole(UserRole.USER);
    setPostId(null);
  }

  public static void requirePositiveFinite(double amount) {
    if (!Double.isFinite(amount) || amount <= 0) {
      throw new IllegalArgumentException("Сумма должна быть положительной и конечной");
    }
  }
}
