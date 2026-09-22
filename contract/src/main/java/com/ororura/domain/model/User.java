package com.ororura.domain.model;

public class User {
  private String name;
  private String homeAddress;
  private String blockchainAddress;
  private long balance;
  private UserRole role = UserRole.USER;
  private String postId;

  public User(
      String name, String homeAddress, String blockchainAddress, long balance, UserRole role) {
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

  public long getBalance() {
    return balance;
  }

  public void setBalance(long balance) {
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

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
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

  public void debit(long amount) {
    requirePositive(amount);
    if (balance < amount) {
      throw new IllegalStateException("Недостаточно средств");
    }
    long result = Math.subtractExact(balance, amount);
    balance = result;
  }

  public void credit(long amount) {
    requirePositive(amount);
    long result = Math.addExact(balance, amount);
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

  public static void requirePositive(long amount) {
    if (amount <= 0) throw new IllegalArgumentException("Сумма должна быть положительной");
  }
}
