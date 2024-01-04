package com.study.events.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum EventErrors {
  PARTICIPANTS_LIMIT("EVENT_ERRORS_0001", "The limit of participants must be bigger than 0!"),
  INVALID_DATE("EVENT_ERRORS_0002", "The date must be after today!"),
  INVALID_TIME("EVENT_ERRORS_0003", "Invalid time!"),
  EVENT_NOT_FOUND("EVENT_ERRORS_0004", "Event not found!");

  private final String code;
  private final String message;
}
