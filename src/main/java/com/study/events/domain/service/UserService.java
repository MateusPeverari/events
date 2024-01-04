package com.study.events.domain.service;

import com.study.events.application.ports.inbound.UserInputPort;
import com.study.events.application.ports.outbound.UserPersistencePort;
import com.study.events.domain.exception.UserErrors;
import com.study.events.domain.exception.UserException;
import com.study.events.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class UserService implements UserInputPort {
  private final UserPersistencePort userPersistencePort;

  @Override
  public User createUser(User user) {
    log.info("Create user: {}", user);
    return userPersistencePort.save(user);
  }

  @Override
  public User findUserById(String id) {
    log.info("Searching user: {}", id);

    return userPersistencePort.findById(id)
        .orElseThrow(() -> new UserException(UserErrors.USER_NOT_FOUND));
  }

  @Override
  public User updateUser(User user, String id) {
    log.info("Updating user: {}", id);

    return userPersistencePort.update(user, id);
  }

  @Override
  public void deleteUser(User user) {
    log.info("Deleting user: {}", user);
    userPersistencePort.deleteUser(user);
  }
}
