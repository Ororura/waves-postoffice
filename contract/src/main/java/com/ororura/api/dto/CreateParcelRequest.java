package com.ororura.api.dto;

import com.ororura.domain.model.ParcelType;

public class CreateParcelRequest {
  private String trackNumber;
  private String to;
  private ParcelType type;
  private String shippingClass;
  private String deliveryTime;
  private double weight;
  private long declaredValue;
  private String totalValue;
  private String addressTo;
  private String addressFrom;
  private int nextOffice;

  public CreateParcelRequest() {}

  public String getTrackNumber() {
    return trackNumber;
  }

  public void setTrackNumber(String v) {
    trackNumber = v;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String v) {
    to = v;
  }

  public ParcelType getType() {
    return type;
  }

  public void setType(ParcelType v) {
    type = v;
  }

  public String getShippingClass() {
    return shippingClass;
  }

  public void setShippingClass(String v) {
    shippingClass = v;
  }

  public String getDeliveryTime() {
    return deliveryTime;
  }

  public void setDeliveryTime(String v) {
    deliveryTime = v;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double v) {
    weight = v;
  }

  public long getDeclaredValue() {
    return declaredValue;
  }

  public void setDeclaredValue(long v) {
    declaredValue = v;
  }

  public String getTotalValue() {
    return totalValue;
  }

  public void setTotalValue(String v) {
    totalValue = v;
  }

  public String getAddressTo() {
    return addressTo;
  }

  public void setAddressTo(String v) {
    addressTo = v;
  }

  public String getAddressFrom() {
    return addressFrom;
  }

  public void setAddressFrom(String v) {
    addressFrom = v;
  }

  public int getNextOffice() {
    return nextOffice;
  }

  public void setNextOffice(int v) {
    nextOffice = v;
  }
}
