package com.ororura.model;

import com.ororura.api.IPostContract;

import java.util.List;

public class Parcel {
    private String trackNumber;
    private String from;
    private String to;
    private String type;
    private String shippingClass;
    private String deliveryTime;
    private double shippingCost;
    private double weight;
    private double declaredValue = 0;
    private String totalValue;
    private String addressTo;
    private String addressFrom;
    private String currentOffice;
    private List<User> employeeCheckoutParcel;

    public Parcel(String trackNumber, String from, String to, String type, String shippingClass, String deliveryTime, double weight, double declaredValue, String totalValue, String addressTo, String addressFrom, String currentOffice) {
        this.trackNumber = trackNumber;
        this.from = from;
        this.to = to;
        this.type = type;
        this.shippingClass = shippingClass;
        this.deliveryTime = deliveryTime;
        this.weight = weight;
        this.declaredValue = declaredValue;
        this.totalValue = totalValue;
        this.addressTo = addressTo;
        this.addressFrom = addressFrom;
        this.currentOffice = currentOffice;
    }

    public Parcel() {
    }

    public String getCurrentOffice() {
        return currentOffice;
    }

    public void setCurrentOffice(String currentOffice) {
        this.currentOffice = currentOffice;
    }

    public List<User> getEmployeeCheckoutParcel() {
        return employeeCheckoutParcel;
    }

    public void setEmployeeCheckoutParcel(List<User> employeeCheckoutParcel) {
        this.employeeCheckoutParcel = employeeCheckoutParcel;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getShippingClass() {
        return shippingClass;
    }

    public void setShippingClass(String shippingClass) {
        this.shippingClass = shippingClass;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getDeclaredValue() {
        return declaredValue;
    }

    public void setDeclaredValue(double declaredValue) {
        this.declaredValue = declaredValue;
    }

    public String getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(String totalValue) {
        this.totalValue = totalValue;
    }

    public String getAddressTo() {
        return addressTo;
    }

    public void setAddressTo(String addressTo) {
        this.addressTo = addressTo;
    }

    public String getAddressFrom() {
        return addressFrom;
    }

    public void setAddressFrom(String addressFrom) {
        this.addressFrom = addressFrom;
    }
}
