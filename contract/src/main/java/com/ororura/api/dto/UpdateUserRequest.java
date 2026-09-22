package com.ororura.api.dto;

public class UpdateUserRequest {
  private String name;
  private String homeAddress;

  public UpdateUserRequest() {}

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  public String getHomeAddress() {
    return homeAddress;
  }

  public void setHomeAddress(String value) {
    homeAddress = value;
  }
}
