package com.ororura.model;

public class AcceptedParcel {
    private Parcel parcel;
    private User user;

    public AcceptedParcel(Parcel parcel, User user) {
        this.parcel = parcel;
        this.user = user;
    }

    public AcceptedParcel() {
    }

    public Parcel getParcel() {
        return parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
