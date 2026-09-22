package com.ororura.domain.repository;

import com.ororura.domain.model.MoneyTransfer;

public interface TransferRepository {
  int create(MoneyTransfer transfer);

  MoneyTransfer requireById(int id);

  void save(int id, MoneyTransfer transfer);
}
