package com.ororura.domain.repository;
import java.util.Optional;
public interface ContractMetadataRepository {
    Optional<String> findOwner();
    void saveOwner(String address);
}
