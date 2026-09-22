package com.ororura.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostOffice {
  private int postNumber;
  private String officeType;
  private Map<String, User> usersInOffice = new HashMap<>();
  private List<ParcelTransit> transitHistory = new ArrayList<>();

  public PostOffice() {}

  public PostOffice(int postNumber, String officeType) {
    this.postNumber = postNumber;
    this.officeType = officeType;
  }

  public int getPostNumber() {
    return postNumber;
  }

  public void setPostNumber(int value) {
    postNumber = value;
  }

  public String getOfficeType() {
    return officeType;
  }

  public void setOfficeType(String value) {
    officeType = value;
  }

  public Map<String, User> getUsersInOffice() {
    return usersInOffice;
  }

  public void setUsersInOffice(Map<String, User> value) {
    usersInOffice = value == null ? new HashMap<>() : new HashMap<>(value);
  }

  public List<ParcelTransit> getTransitHistory() {
    return List.copyOf(transitHistory);
  }

  public void setTransitHistory(List<ParcelTransit> value) {
    transitHistory = value == null ? new ArrayList<>() : new ArrayList<>(value);
  }

  public void addUser(User user) {
    usersInOffice.put(user.getBlockchainAddress(), user);
  }

  public void recordTransit(ParcelTransit event) {
    if (event == null || event.getFromOfficeId() != postNumber) {
      throw new IllegalArgumentException("Запись относится к другому отделению");
    }
    transitHistory.add(event);
  }
}
