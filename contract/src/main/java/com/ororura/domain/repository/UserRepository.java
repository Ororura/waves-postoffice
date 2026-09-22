package com.ororura.domain.repository;

import com.ororura.domain.model.User;
import java.util.Optional;

public interface UserRepository {
  Optional<User> findByAddress(String address);

  void save(User user);
}
