package com.ororura.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostOffice {
    private int postNumber;
    private String officeType;
    private final HashMap<String, User> usersInOffice = new HashMap();
    private final List<AcceptedParcel> acceptedParcel = new ArrayList<>();

    public PostOffice(int postNumber, String officeType) {
        this.postNumber = postNumber;
        this.officeType = officeType;
    }

    public PostOffice() {
    }

    public int getPostNumber() {
        return postNumber;
    }

    public void setPostNumber(int postNumber) {
        this.postNumber = postNumber;
    }

    public String getOfficeType() {
        return officeType;
    }

    public void setOfficeType(String officeType) {
        this.officeType = officeType;
    }

    public List<AcceptedParcel> getAcceptedParcel() {
        return acceptedParcel;
    }

    public HashMap<String, User> getUsersInOffice() {
        return usersInOffice;
    }

    public void addUser(User user) {
        this.usersInOffice.put(user.getBlockchainAddress(), user);
    }
}
