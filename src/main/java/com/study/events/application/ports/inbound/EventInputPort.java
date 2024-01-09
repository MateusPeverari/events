package com.study.events.application.ports.inbound;

import com.study.events.domain.model.Event;
import com.study.events.infrastructure.adapters.data.EventAddUserRequest;

public interface EventInputPort {
  Event createEvent(Event event);

  Event updateEvent(Event event, String eventId);

  Event findById(String eventId);

  void deleteEvent(String eventId);

  int addUserToEvent(String eventId, EventAddUserRequest eventAddUserRequest);
}
