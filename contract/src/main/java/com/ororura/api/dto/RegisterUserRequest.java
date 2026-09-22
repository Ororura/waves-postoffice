package com.ororura.api.dto;

public class RegisterUserRequest {
  private String name;
  private String homeAddress;

  public RegisterUserRequest() {}

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
