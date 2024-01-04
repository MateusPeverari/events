package com.study.events.domain.exception;

import lombok.Data;

@Data
public class UserException extends RuntimeException {
  private UserErrors userErrors;

  public UserException(UserErrors errors) {
    this.userErrors = errors;
  }
}
