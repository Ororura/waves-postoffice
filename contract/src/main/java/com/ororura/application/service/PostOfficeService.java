package com.ororura.application.service;

import com.ororura.domain.model.PostOffice;
import com.ororura.domain.repository.PostOfficeRepository;
import java.util.HashMap;

public final class PostOfficeService {
  private final PostOfficeRepository repository;

  public PostOfficeService(PostOfficeRepository repository) {
    this.repository = repository;
  }

  public PostOffice requireOffice(int id) {
    PostOffice office = repository.findAll().get(id);
    if (office == null) throw new IllegalArgumentException("Отделение не найдено: " + id);
    return office;
  }

  public HashMap<Integer, PostOffice> all() {
    return repository.findAll();
  }

  public void saveAll(HashMap<Integer, PostOffice> offices) {
    repository.saveAll(offices);
  }
}
