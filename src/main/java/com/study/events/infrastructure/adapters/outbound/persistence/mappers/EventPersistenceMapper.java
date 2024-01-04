package com.study.events.infrastructure.adapters.outbound.persistence.mappers;

import com.study.events.domain.model.Event;
import com.study.events.infrastructure.adapters.outbound.persistence.entity.EventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface EventPersistenceMapper {
  EventEntity toEventEntity(Event event);

  @Mapping(source = "owner.id", target = "ownerId")
  Event toEvent(EventEntity eventEntity);
}
