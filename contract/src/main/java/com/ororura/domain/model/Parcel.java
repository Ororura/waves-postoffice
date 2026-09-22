package com.ororura.domain.model;

import java.util.List;

public class Parcel {
  private String trackNumber;
  private String from;
  private String to;
  private ParcelType type;
  private String shippingClass;
  private String deliveryTime;
  private long shippingCost;
  private double weight;
  private long declaredValue = 0;
  private String totalValue;
  private String addressTo;
  private String addressFrom;
  private int nextOffice;
  private List<User> employeeCheckoutParcel;
  private ParcelStatus status;

  public Parcel(
      String trackNumber,
      String from,
      String to,
      ParcelType type,
      String shippingClass,
      String deliveryTime,
      double weight,
      long declaredValue,
      String totalValue,
      String addressTo,
      String addressFrom,
      int nextOffice) {
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
    this.nextOffice = nextOffice;
  }

  public Parcel() {}

  public ParcelStatus getStatus() {
    return status;
  }

  public void setStatus(ParcelStatus status) {
    this.status = status;
  }

  public int getNextOffice() {
    return nextOffice;
  }

  public void setNextOffice(int nextOffice) {
    this.nextOffice = nextOffice;
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

  public ParcelType getType() {
    return type;
  }

  public void setType(ParcelType type) {
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

  public long getShippingCost() {
    return shippingCost;
  }

  public void setShippingCost(long shippingCost) {
    this.shippingCost = shippingCost;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public long getDeclaredValue() {
    return declaredValue;
  }

  public void setDeclaredValue(long declaredValue) {
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

  public void assignSender(String sender) {
    if (sender == null || sender.isBlank()) {
      throw new IllegalArgumentException("Не указан отправитель");
    }
    this.from = sender;
  }

  public void assignShippingCost(long amount) {
    User.requirePositive(amount);
    this.shippingCost = amount;
  }

  public void routeToOffice(int officeId) {
    if (officeId <= 0) {
      throw new IllegalArgumentException("Некорректный идентификатор отделения");
    }
    if (status != ParcelStatus.ACCEPTED && status != ParcelStatus.IN_TRANSIT) {
      throw new IllegalStateException("Посылка не находится в процессе доставки");
    }
    this.nextOffice = officeId;
    this.status = ParcelStatus.IN_TRANSIT;
  }
}
