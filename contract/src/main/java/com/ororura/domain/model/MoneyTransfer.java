package com.ororura.domain.model;

public class MoneyTransfer {
  private String from;
  private String to;
  private long amount;
  private int lifeTime;
  private boolean active = true;

  public MoneyTransfer(String from, String to, long amount, int lifeTime) {
    this.from = from;
    this.to = to;
    this.amount = amount;
    this.lifeTime = lifeTime;
  }

  public MoneyTransfer() {}

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public long getAmount() {
    return amount;
  }

  public void setAmount(long amount) {
    this.amount = amount;
  }

  public int getLifeTime() {
    return lifeTime;
  }

  public void setLifeTime(int lifeTime) {
    this.lifeTime = lifeTime;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public static MoneyTransfer create(String sender, String recipient, long amount, int lifeTime) {
    if (sender == null
        || sender.isBlank()
        || recipient == null
        || recipient.isBlank()
        || sender.equals(recipient)) {
      throw new IllegalArgumentException("Некорректные участники перевода");
    }
    User.requirePositive(amount);
    MoneyTransfer transfer = new MoneyTransfer();
    transfer.setFrom(sender);
    transfer.setTo(recipient);
    transfer.setAmount(amount);
    transfer.setLifeTime(lifeTime);
    transfer.setActive(true);
    return transfer;
  }

  public void requireRecipient(String caller) {
    if (!to.equals(caller)) {
      throw new SecurityException("Нельзя обработать чужой перевод");
    }
  }

  public void requireActive() {
    if (!active) {
      throw new IllegalStateException("Перевод уже обработан");
    }
  }

  public void accept() {
    requireActive();
    active = false;
  }

  public void reject() {
    requireActive();
    active = false;
  }
}
