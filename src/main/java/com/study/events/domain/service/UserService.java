package com.study.events.domain.service;

import com.study.events.application.ports.inbound.UserInputPort;
import com.study.events.application.ports.outbound.UserPersistencePort;
import com.study.events.domain.exception.UserErrors;
import com.study.events.domain.exception.UserException;
import com.study.events.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
public class UserService implements UserInputPort {
  private final UserPersistencePort userPersistencePort;

  @Override
  @Transactional
  public User createUser(User user) {
    // Persisting the user within a dedicated transaction makes the write inexpensive to retry and
    // avoids holding on to pooled connections when the system is under heavy sign-up load.
    log.info("Create user: {}", user);
    return userPersistencePort.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public User findUserById(String id) {
    // Marking the lookup as read-only lets Hibernate serve the request without tracking changes,
    // which keeps the session lightweight and increases the number of concurrent lookups we can
    // handle before exhausting memory or CPU.
    log.info("Searching user: {}", id);

    return userPersistencePort.findById(id)
        .orElseThrow(() -> new UserException(UserErrors.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public User updateUser(User user, String id) {
    // Serialising the update through a transaction ensures the record is refreshed atomically,
    // keeping concurrent updates from stepping on one another.
    log.info("Updating user: {}", id);

    return userPersistencePort.update(user, id);
  }

  @Override
  @Transactional
  public void deleteUser(User user) {
    // Enclosing deletions in a transaction encourages Hibernate to reuse the same JDBC connection
    // for cascade operations, which lowers lock contention when many accounts are removed
    // simultaneously.
    log.info("Deleting user: {}", user);
    userPersistencePort.deleteUser(user);
  }
}
