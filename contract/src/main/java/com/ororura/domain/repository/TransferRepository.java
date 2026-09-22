package com.ororura.domain.repository;

import com.ororura.domain.model.MoneyTransfer;
import java.util.List;

public interface TransferRepository {
  List<MoneyTransfer> findAll();

  void saveAll(List<MoneyTransfer> transfers);
}
