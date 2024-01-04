package com.study.events.infrastructure.adapters.outbound.persistence;

import com.study.events.application.ports.outbound.UserPersistencePort;
import com.study.events.domain.exception.UserErrors;
import com.study.events.domain.exception.UserException;
import com.study.events.domain.model.User;
import com.study.events.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import com.study.events.infrastructure.adapters.outbound.persistence.mappers.UserPersistenceMapper;
import com.study.events.infrastructure.adapters.outbound.persistence.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

  private final UserRepository userRepository;
  private final UserPersistenceMapper userPersistenceMapper;

  @Override
  public User save(User user) {
    var userEntity = userRepository.save(userPersistenceMapper.toUserEntity(user));
    return userPersistenceMapper.toUser(userEntity);
  }

  @Override
  public Optional<User> findById(String id) {
    Optional<UserEntity> userOptional = userRepository.findById(UUID.fromString(id));
    return userOptional.map(userPersistenceMapper::toUser);
  }

  @Override
  public User update(User user, String id) {
    var userSavedOptional = userRepository.findById(UUID.fromString(id));

    if (userSavedOptional.isPresent()) {
      var userSaved = userSavedOptional.get();
      user.setId(userSaved.getId());
      var userEntity = userRepository.save(userPersistenceMapper.toUserEntity(user));
      return userPersistenceMapper.toUser(userEntity);
    } else {
      throw new UserException(UserErrors.USER_NOT_FOUND);
    }
  }

  @Override
  public void deleteUser(User user) {
    userRepository.delete(userPersistenceMapper.toUserEntity(user));
  }

  @Override
  public Optional<User> findByCpf(String cpf) {
    Optional<UserEntity> userOptional = userRepository.searchByCPF(cpf);
    return userOptional.map(userPersistenceMapper::toUser);
  }
}
