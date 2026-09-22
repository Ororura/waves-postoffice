package com.ororura.application.service;

import com.ororura.application.context.ContractContext;
import com.ororura.domain.model.PostOffice;
import com.ororura.domain.repository.ContractMetadataRepository;
import com.ororura.domain.repository.PostOfficeRepository;
import java.util.HashMap;

public final class InitializationService {
  private final ContractMetadataRepository metadata;
  private final PostOfficeRepository offices;
  private final ContractContext context;

  public InitializationService(
      ContractMetadataRepository metadata, PostOfficeRepository offices, ContractContext context) {
    this.metadata = metadata;
    this.offices = offices;
    this.context = context;
  }

  public void init() {
    if (metadata.findOwner().isPresent())
      throw new IllegalStateException("Контракт уже инициализирован");
    HashMap<Integer, PostOffice> all = new HashMap<>();
    all.put(344000, new PostOffice(344000, "SORTING_CENTER"));
    all.put(347900, new PostOffice(347900, "MAIN_POST_OFFICE"));
    all.put(347901, new PostOffice(347901, "POST_OFFICE"));
    all.put(347902, new PostOffice(347902, "POST_OFFICE"));
    all.put(347903, new PostOffice(347903, "POST_OFFICE"));
    all.put(346770, new PostOffice(346770, "MAIN_POST_OFFICE"));
    all.put(346771, new PostOffice(346771, "POST_OFFICE"));
    offices.saveAll(all);
    metadata.saveOwner(context.caller());
  }
}
