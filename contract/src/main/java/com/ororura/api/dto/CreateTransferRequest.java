package com.ororura.api.dto;

public class CreateTransferRequest {
  private String to;
  private long amount;

  public CreateTransferRequest() {}

  public String getTo() {
    return to;
  }

  public void setTo(String value) {
    to = value;
  }

  /** Amount in hundredths of a token. */
  public long getAmount() {
    return amount;
  }

  public void setAmount(long value) {
    amount = value;
  }
}
