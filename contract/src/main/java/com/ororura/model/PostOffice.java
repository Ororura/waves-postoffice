package com.ororura.model;

import java.util.ArrayList;
import java.util.List;

public class PostOffice {
    private int postNumber;
    private String officeType;
    //private final List<User> usersInOffice = new ArrayList<>();
    private final List<Parcel> acceptedParcel = new ArrayList<>();

    public PostOffice(int postNumber, String officeType) {
        this.postNumber = postNumber;
        this.officeType = officeType;
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

    public List<Parcel> getAcceptedParcel() {
        return acceptedParcel;
    }
}
