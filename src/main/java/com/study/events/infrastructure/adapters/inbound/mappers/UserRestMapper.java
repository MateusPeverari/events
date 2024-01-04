package com.study.events.infrastructure.adapters.inbound.mappers;

import com.study.events.domain.model.User;
import com.study.events.infrastructure.adapters.data.UserCreateRequest;
import com.study.events.infrastructure.adapters.data.UserResponse;
import org.mapstruct.Mapper;

@Mapper
public interface UserRestMapper {
  User toUser(UserCreateRequest userCreateRequest);

  UserResponse toUserResponse(User user);
}
