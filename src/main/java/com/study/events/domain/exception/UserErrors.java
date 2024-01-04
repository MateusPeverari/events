package com.study.events.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum UserErrors {
  USER_NOT_FOUND("USER_ERRORS_0001", "User not found!"),
  USER_CPF_INVALID("USER_ERRORS_0002", "CPF is invalid!");

  private final String code;
  private final String message;
}
