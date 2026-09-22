package com.ororura.domain.model;

/** A deterministic parcel transit record. Sequence is local to the parcel. */
public class ParcelTransit {
  private String trackingNumber;
  private int fromOfficeId;
  private int toOfficeId;
  private String employeeAddress;
  private int sequence;

  public ParcelTransit() {}

  public ParcelTransit(
      String trackingNumber,
      int fromOfficeId,
      int toOfficeId,
      String employeeAddress,
      int sequence) {
    this.trackingNumber = trackingNumber;
    this.fromOfficeId = fromOfficeId;
    this.toOfficeId = toOfficeId;
    this.employeeAddress = employeeAddress;
    this.sequence = sequence;
  }

  public String getTrackingNumber() {
    return trackingNumber;
  }

  public void setTrackingNumber(String value) {
    trackingNumber = value;
  }

  public int getFromOfficeId() {
    return fromOfficeId;
  }

  public void setFromOfficeId(int value) {
    fromOfficeId = value;
  }

  public int getToOfficeId() {
    return toOfficeId;
  }

  public void setToOfficeId(int value) {
    toOfficeId = value;
  }

  public String getEmployeeAddress() {
    return employeeAddress;
  }

  public void setEmployeeAddress(String value) {
    employeeAddress = value;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int value) {
    sequence = value;
  }
}
