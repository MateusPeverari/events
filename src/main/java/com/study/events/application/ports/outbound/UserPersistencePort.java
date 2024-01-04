package com.study.events.application.ports.outbound;

import com.study.events.domain.model.User;
import java.util.Optional;

public interface UserPersistencePort {
  User save(User user);

  Optional<User> findById(String id);

  User update(User user, String id);

  void deleteUser(User user);

  Optional<User> findByCpf(String cpf);
}
