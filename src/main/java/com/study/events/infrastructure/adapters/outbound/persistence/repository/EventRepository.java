package com.study.events.infrastructure.adapters.outbound.persistence.repository;

import com.study.events.infrastructure.adapters.outbound.persistence.entity.EventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
}
