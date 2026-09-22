package com.ororura.domain.repository;
import com.ororura.domain.model.PostOffice;
import java.util.HashMap;
public interface PostOfficeRepository {
    HashMap<Integer, PostOffice> findAll();
    void saveAll(HashMap<Integer, PostOffice> offices);
}
