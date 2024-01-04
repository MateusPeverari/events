package com.study.events.domain.service;

import com.study.events.application.ports.inbound.EventInputPort;
import com.study.events.application.ports.outbound.EventPersistencePort;
import com.study.events.application.ports.outbound.UserPersistencePort;
import com.study.events.domain.exception.EventErrors;
import com.study.events.domain.exception.EventException;
import com.study.events.domain.exception.UserErrors;
import com.study.events.domain.exception.UserException;
import com.study.events.domain.model.Event;
import com.study.events.infrastructure.adapters.data.EventAddUserRequest;
import com.study.events.infrastructure.adapters.outbound.persistence.mappers.UserPersistenceMapper;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class EventService implements EventInputPort {
  private final EventPersistencePort eventPersistencePort;
  private final UserPersistencePort userPersistencePort;
  private final UserPersistenceMapper userPersistenceMapper;

  @Override
  public Event createEvent(Event event) {
    log.info("Create event: {}", event);

    var userOptional = userPersistencePort.findById(event.getOwnerId().toString());
    if (userOptional.isEmpty()) {
      throw new UserException(UserErrors.USER_NOT_FOUND);
    }

    validateEvent(event);

    event.setOwner(userPersistenceMapper.toUserEntity(userOptional.get()));
    event.setCreatedAt(LocalDateTime.now());

    return eventPersistencePort.save(event);
  }

  @Override
  public Event updateEvent(Event event, String eventId) {
    log.info("Update event with Id: {}", eventId);

    var eventOptional = eventPersistencePort.findById(eventId);
    if (eventOptional.isEmpty()) {
      throw new EventException(EventErrors.EVENT_NOT_FOUND);
    }

    var userOptional = userPersistencePort.findById(event.getOwnerId().toString());
    if (userOptional.isEmpty()) {
      throw new UserException(UserErrors.USER_NOT_FOUND);
    }

    validateEvent(event);

    event.setOwner(userPersistenceMapper.toUserEntity(userOptional.get()));
    event.setModifiedAt(LocalDateTime.now());

    return eventPersistencePort.update(event, eventId);
  }

  @Override
  public Event findById(String eventId) {
    log.info("Searching event: {}", eventId);

    return eventPersistencePort.findById(eventId)
        .orElseThrow(() -> new EventException(EventErrors.EVENT_NOT_FOUND));
  }

  @Override
  public void deleteEvent(String eventId) {
    eventPersistencePort.delete(eventId);
  }

  @Override
  public void addUserToEvent(String eventId, EventAddUserRequest eventAddUserRequest) {
    log.info("Adding user " + eventAddUserRequest + " to event" + eventId);

    var event = findById(eventId);
    if (event.getAttendees() == event.getParticipantsLimit()) {
      throw new EventException(EventErrors.PARTICIPANTS_LIMIT);
    }

    var userOptional = userPersistencePort.findByCpf(eventAddUserRequest.getCpf());

    if (userOptional.isEmpty()) {
      throw new UserException(UserErrors.USER_NOT_FOUND);
    }

    var user = userOptional.get();

    event.setAttendees(event.getAttendees() + 1);
    var attendeesList = event.getAttendeesList();
    attendeesList.add(user);

    eventPersistencePort.update(event, eventId);
  }

  private void validateEvent(Event event) {
    log.info("Validating event: {}", event);

    validateParticipants(event);
    validateDate(event);
    validateTime(event);
  }

  private void validateTime(Event event) {
    log.info("Validating event time {}", event.getEventTime());

    var time = event.getEventTime();
    try {
      Time.valueOf(time);
    } catch (Exception e) {
      throw new EventException(EventErrors.INVALID_TIME);
    }
  }

  private void validateDate(Event event) {
    log.info("Validating event date {}", event.getEventDate());

    var date = event.getEventDate();
    if (date.compareTo(Date.valueOf(LocalDate.now())) < 0) {
      throw new EventException(EventErrors.INVALID_DATE);
    }
  }

  private void validateParticipants(Event event) {
    log.info("Validating event participants limit {}", event.getParticipantsLimit());

    var participants = event.getParticipantsLimit();
    if (participants <= 0) {
      throw new EventException(EventErrors.PARTICIPANTS_LIMIT);
    }
  }
}
