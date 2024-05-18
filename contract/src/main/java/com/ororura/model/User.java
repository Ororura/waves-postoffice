package com.ororura.model;

import static com.ororura.api.IPostContract.Role.USER;

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

    public User() {
    }

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
        this.postId = "RR" + postId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
