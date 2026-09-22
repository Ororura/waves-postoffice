package com.ororura.infrastructure.blockchain;

import com.ororura.application.context.ContractContext;
import com.wavesenterprise.sdk.contract.api.domain.ContractCall;

public final class WavesContractContext implements ContractContext {
  private final ContractCall call;

  public WavesContractContext(ContractCall call) {
    this.call = call;
  }

  @Override
  public String caller() {
    return call.getCaller();
  }
}
