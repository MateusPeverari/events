package com.study.events.infrastructure.adapters.outbound.persistence.repository;

import com.study.events.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> searchByCPF(@CPF(message = "CPF inválido") String cpf);
}
