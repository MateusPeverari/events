package com.study.events.application.ports.inbound;

import com.study.events.domain.model.User;

public interface UserInputPort {
  User createUser(User user);

  User findUserById(String id);

  User updateUser(User user, String id);

  void deleteUser(User user);
}
