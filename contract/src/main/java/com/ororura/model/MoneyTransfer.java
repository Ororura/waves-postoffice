package com.ororura.model;

public class MoneyTransfer {
    private String from;
    private String to;
    private double amount;
    private int lifeTime;
    private boolean active = true;

    public MoneyTransfer(String from, String to, double amount, int lifeTime) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.lifeTime = lifeTime;
    }

    public MoneyTransfer() {
    }

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
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
}
