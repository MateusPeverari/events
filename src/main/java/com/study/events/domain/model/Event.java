package com.study.events.domain.model;

import com.study.events.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class Event {
  private UUID id;
  private UUID ownerId;
  private UserEntity owner;
  private String eventName;
  private Date eventDate;
  private String eventTime;
  private int participantsLimit;
  private int attendees;
  private List<User> attendeesList;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
}
