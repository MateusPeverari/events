package com.study.events.infrastructure.adapters.inbound.mappers;

import com.study.events.domain.model.Event;
import com.study.events.infrastructure.adapters.data.EventCreateRequest;
import com.study.events.infrastructure.adapters.data.EventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface EventRestMapper {
  @Mapping(source = "date", target = "eventDate")
  @Mapping(source = "time", target = "eventTime")
  Event toEvent(EventCreateRequest eventCreateRequest);

  @Mapping(source = "eventDate", target = "date")
  @Mapping(source = "eventTime", target = "time")
  @Mapping(source = "id", target = "eventId")
  EventResponse toEventResponse(Event event);
}
