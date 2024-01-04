package com.study.events.infrastructure.adapters.outbound.persistence.mappers;

import com.study.events.domain.model.User;
import com.study.events.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserPersistenceMapper {
  @Mapping(source = "cpf", target =  "CPF")
  UserEntity toUserEntity(User user);

  @Mapping(source = "CPF", target = "cpf")
  @Mapping(source = "id", target = "id")
  User toUser(UserEntity userEntity);
}
