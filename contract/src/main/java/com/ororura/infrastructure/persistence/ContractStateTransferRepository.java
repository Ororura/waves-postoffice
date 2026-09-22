package com.ororura.infrastructure.persistence;
import com.ororura.domain.model.MoneyTransfer;
import com.ororura.domain.repository.TransferRepository;
import com.wavesenterprise.sdk.contract.api.state.ContractState;
import com.wavesenterprise.sdk.contract.api.state.TypeReference;
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping;
import java.util.List;
public final class ContractStateTransferRepository implements TransferRepository {
    private final Mapping<List<MoneyTransfer>> mapping;
    public ContractStateTransferRepository(ContractState state) {
        mapping = state.getMapping(new TypeReference<List<MoneyTransfer>>() {}, "TRANSFER_MONEY_MAPPING");
    }
    @Override public List<MoneyTransfer> findAll() {
        return mapping.tryGet("_").orElseThrow(() ->
            new IllegalStateException("Переводы не инициализированы"));
    }
    @Override public void saveAll(List<MoneyTransfer> transfers) {
        mapping.put("_", transfers);
    }
}
