package com.study.events.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GenericErrors {
  USER_GENERIC_ERROR("USER_ERROR_9999", "Generic Error"),
  USER_VALIDATION_ERROR("USER_ERROR_9998", "Validation Error");

  private String errorCode;
  private String message;
}
