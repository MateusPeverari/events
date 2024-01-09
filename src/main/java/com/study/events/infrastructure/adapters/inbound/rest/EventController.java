package com.study.events.infrastructure.adapters.inbound.rest;

import com.study.events.application.ports.inbound.EventInputPort;
import com.study.events.infrastructure.adapters.EventApi;
import com.study.events.infrastructure.adapters.data.DeleteResponse;
import com.study.events.infrastructure.adapters.data.EventAddUserRequest;
import com.study.events.infrastructure.adapters.data.EventAddUserResponse;
import com.study.events.infrastructure.adapters.data.EventCreateRequest;
import com.study.events.infrastructure.adapters.data.EventResponse;
import com.study.events.infrastructure.adapters.inbound.mappers.EventRestMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EventController implements EventApi {
  private final EventRestMapper eventRestMapper;
  private final EventInputPort eventInputPort;

  @Override
  public ResponseEntity<EventResponse> addEvent(EventCreateRequest eventCreateRequest) {
    log.info("Request to create event: {}", eventCreateRequest);

    var eventCreated = eventInputPort.createEvent(eventRestMapper.toEvent(eventCreateRequest));
    var eventResponse = eventRestMapper.toEventResponse(eventCreated);
    eventResponse.setOwnerId(eventCreateRequest.getOwnerId());

    log.info("Event created, sending response: {}", eventResponse);
    return ResponseEntity.status(HttpStatus.CREATED).body(eventResponse);
  }

  @Override
  public ResponseEntity<EventResponse> updateUser(String eventId,
                                                  EventCreateRequest eventCreateRequest) {
    log.info("Request to update event: {}", eventCreateRequest);
    var eventUpdated =
        eventInputPort.updateEvent(eventRestMapper.toEvent(eventCreateRequest), eventId);
    var eventResponse = eventRestMapper.toEventResponse(eventUpdated);
    eventResponse.setOwnerId(eventCreateRequest.getOwnerId());
    log.info("Event updated, sending response: {}", eventResponse);
    return ResponseEntity.status(HttpStatus.CREATED).body(eventResponse);
  }

  @Override
  public ResponseEntity<EventResponse> findEventById(String eventId) {
    log.info("Searching event {}", eventId);
    var event = eventInputPort.findById(eventId);
    var eventResponse = eventRestMapper.toEventResponse(event);
    return ResponseEntity.ok(eventResponse);
  }

  @Override
  public ResponseEntity<DeleteResponse> deleteEvent(String eventId) {
    log.info("Deleting event with id {}", eventId);
    eventInputPort.deleteEvent(eventId);

    var eventDelete = new DeleteResponse();
    eventDelete.setMessage("Event " + eventId + " deleted successfully!");
    eventDelete.setDeletedAt(LocalDateTime.now());

    return ResponseEntity.ok(eventDelete);
  }

  @Override
  public ResponseEntity<EventAddUserResponse> addUserEvent(String eventId,
                                                           EventAddUserRequest eventAddUserRequest) {
    log.info("Add user to event with id: ", eventId);
    var result = eventInputPort.addUserToEvent(eventId, eventAddUserRequest);

    var userAdd = new EventAddUserResponse();
    userAdd.setAddedAt(LocalDateTime.now());
    if (result > 0) {
      userAdd.setMessage("User " + eventAddUserRequest.getCpf() + " was added to event: " + eventId);
    } else {
      userAdd.setMessage("User " + eventAddUserRequest.getCpf() + " was already signed up to the event: " + eventId + ". The user has now been removed.");
    }
    return ResponseEntity.ok(userAdd);
  }
}
