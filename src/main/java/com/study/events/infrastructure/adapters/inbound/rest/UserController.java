package com.study.events.infrastructure.adapters.inbound.rest;

import com.study.events.application.ports.inbound.UserInputPort;
import com.study.events.infrastructure.adapters.UserApi;
import com.study.events.infrastructure.adapters.data.DeleteResponse;
import com.study.events.infrastructure.adapters.data.UserCreateRequest;
import com.study.events.infrastructure.adapters.data.UserResponse;
import com.study.events.infrastructure.adapters.inbound.mappers.UserRestMapper;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserApi {
  private final UserRestMapper userRestMapper;
  private final UserInputPort userInputPort;

  @Override
  public ResponseEntity<UserResponse> addUser(@Valid UserCreateRequest userCreateRequest) {
    log.info("Request to create user: {}", userCreateRequest);
    var userCreated = userInputPort.createUser(userRestMapper.toUser(userCreateRequest));
    var userResponse = userRestMapper.toUserResponse(userCreated);
    userResponse.setCreateAt(LocalDateTime.now());
    log.info("User created, sending response: {}", userResponse);
    return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
  }

  @Override
  public ResponseEntity<UserResponse> findByUserId(String userId) {
    log.info("Request to search user: {}", userId);
    var user = userInputPort.findUserById(userId);
    var userResponse = userRestMapper.toUserResponse(user);
    log.info("Sending response: {}", userResponse);
    return ResponseEntity.ok(userResponse);
  }

  @Override
  public ResponseEntity<UserResponse> updateByUserId(String userId,
                                                     UserCreateRequest userCreateRequest) {
    log.info("Request to update user: {} {}", userId, userCreateRequest);
    var userUpdated = userInputPort.updateUser(userRestMapper.toUser(userCreateRequest), userId);
    var userResponse = userRestMapper.toUserResponse(userUpdated);
    userResponse.setCreateAt(LocalDateTime.now());
    log.info("User updated, sending response: {}", userResponse);
    return ResponseEntity.ok(userResponse);
  }

  @Override
  public ResponseEntity<DeleteResponse> deleteUser(String userId) {
    log.info("Request to delete user: {}", userId);

    var user = userInputPort.findUserById(userId);
    userInputPort.deleteUser(user);
    var userResponse = new DeleteResponse();
    userResponse.setDeletedAt(LocalDateTime.now());
    userResponse.setMessage("User deleted successfully!");
    return ResponseEntity.ok(userResponse);
  }
}
