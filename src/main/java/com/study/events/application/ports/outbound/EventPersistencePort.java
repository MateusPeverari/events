package com.study.events.application.ports.outbound;

import com.study.events.domain.model.Event;
import java.util.Optional;

public interface EventPersistencePort {
  Event save(Event event);

  Optional<Event> findById(String eventId);

  Event update(Event event, String eventId);

  void delete(String eventId);
}
