package com.study.events.infrastructure.adapters.outbound.persistence;

import com.study.events.application.ports.outbound.EventPersistencePort;
import com.study.events.domain.exception.EventErrors;
import com.study.events.domain.exception.EventException;
import com.study.events.domain.model.Event;
import com.study.events.infrastructure.adapters.outbound.persistence.mappers.EventPersistenceMapper;
import com.study.events.infrastructure.adapters.outbound.persistence.repository.EventRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EventPersistenceAdapter implements EventPersistencePort {
  private final EventRepository eventRepository;
  private final EventPersistenceMapper eventPersistenceMapper;

  @Override
  public Event save(Event event) {
    var eventEntity = eventPersistenceMapper.toEventEntity(event);
    eventEntity.setOwner(event.getOwner());
    eventEntity = eventRepository.save(eventEntity);
    return eventPersistenceMapper.toEvent(eventEntity);
  }

  @Override
  public Optional<Event> findById(String eventId) {
    var eventOptional = eventRepository.findById(UUID.fromString(eventId));
    log.info("event optional: {}", eventOptional);
    return eventOptional.map(eventPersistenceMapper::toEvent);
  }

  @Override
  public Event update(Event event, String eventId) {
    var eventSavedOptional = eventRepository.findById(UUID.fromString(eventId));

    if (eventSavedOptional.isPresent()) {
      var eventSaved = eventSavedOptional.get();

      event.setId(eventSaved.getId());
      var eventEntity = eventPersistenceMapper.toEventEntity(event);
      eventEntity = eventRepository.save(eventEntity);

      return eventPersistenceMapper.toEvent(eventEntity);
    } else {
      throw new EventException(EventErrors.EVENT_NOT_FOUND);
    }
  }

  @Override
  public void delete(String eventId) {
    var eventSavedOptional = findById(eventId);
    if (eventSavedOptional.isEmpty()) {
      throw new EventException(EventErrors.EVENT_NOT_FOUND);
    }
    var eventSaved = eventSavedOptional.get();
    var eventEntity = eventPersistenceMapper.toEventEntity(eventSaved);
    eventRepository.delete(eventEntity);
  }
}
