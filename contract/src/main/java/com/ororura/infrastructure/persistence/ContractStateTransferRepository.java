package com.ororura.infrastructure.persistence;

import com.ororura.domain.model.MoneyTransfer;
import com.ororura.domain.repository.TransferRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;

public final class ContractStateTransferRepository implements TransferRepository {
  private final Mapping<MoneyTransfer> transfers;
  private final Mapping<Integer> counter;

  public ContractStateTransferRepository(ContractState state) {
    transfers = state.getMapping(new TypeReference<MoneyTransfer>() {}, "TRANSFER_V2");
    counter = state.getMapping(new TypeReference<Integer>() {}, "TRANSFER_COUNTER_V2");
  }

  @Override
  public int create(MoneyTransfer transfer) {
    int id = counter.tryGet("NEXT").orElse(0);
    int next = Math.addExact(id, 1);
    if (transfers.tryGet(Integer.toString(id)).isPresent()) {
      throw new IllegalStateException("Идентификатор перевода уже занят");
    }
    transfers.put(Integer.toString(id), transfer);
    counter.put("NEXT", next);
    return id;
  }

  @Override
  public MoneyTransfer requireById(int id) {
    if (id < 0) throw new IllegalArgumentException("Некорректный идентификатор перевода");
    return transfers
        .tryGet(Integer.toString(id))
        .orElseThrow(() -> new IllegalArgumentException("Перевод не найден: " + id));
  }

  @Override
  public void save(int id, MoneyTransfer transfer) {
    if (id < 0 || transfers.tryGet(Integer.toString(id)).isEmpty()) {
      throw new IllegalArgumentException("Перевод не найден: " + id);
    }
    transfers.put(Integer.toString(id), transfer);
  }
}
