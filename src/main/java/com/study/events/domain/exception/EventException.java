package com.study.events.domain.exception;

import lombok.Data;

@Data
public class EventException extends RuntimeException {
  private EventErrors eventErrors;

  public EventException(EventErrors errors) {
    this.eventErrors = errors;
  }
}
