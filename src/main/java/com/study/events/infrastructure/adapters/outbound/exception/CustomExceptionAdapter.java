package com.study.events.infrastructure.adapters.outbound.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.study.events.domain.exception.EventException;
import com.study.events.domain.exception.GenericErrors;
import com.study.events.domain.exception.UserException;
import com.study.events.infrastructure.adapters.data.ErrorResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
@RestController
public class CustomExceptionAdapter extends ResponseEntityExceptionHandler {
  public static final String HANDLING_EXCEPTION_MESSAGE = "Handling the exception. Message: {}";

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                HttpHeaders headers,
                                                                HttpStatusCode status,
                                                                WebRequest request) {
    List<String> errors = new ArrayList<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->

        errors.add(error.getField() + ": " + error.getDefaultMessage())
    );

    ErrorResponse exceptionResponse = new ErrorResponse();
    exceptionResponse.setCode(GenericErrors.USER_VALIDATION_ERROR.getErrorCode());
    exceptionResponse.setReleaseAt(LocalDateTime.now(ZoneId.of("UTC")));
    exceptionResponse.setMessage(GenericErrors.USER_VALIDATION_ERROR.getMessage());
    exceptionResponse.setDetails(errors);
    log.warn("Validation Error: {}", exceptionResponse);
    return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                HttpHeaders headers,
                                                                HttpStatusCode httpStatus,
                                                                WebRequest request) {
    log.warn(HANDLING_EXCEPTION_MESSAGE, ex.getMessage());

    List<String> errors = new ArrayList<>();

    try {
      InvalidFormatException exception = (InvalidFormatException) ex.getCause();

      errors = exception.getPath().stream().map(reference ->
          reference.getFieldName() + ": " + exception.getValue()).toList();
    } catch (Exception ex1) {
      JsonParseException exception = (JsonParseException) ex.getCause();
      errors = Arrays.asList(exception.getOriginalMessage());
    }


    ErrorResponse exceptionResponse = new ErrorResponse();
    exceptionResponse.setCode(GenericErrors.USER_VALIDATION_ERROR.getErrorCode());
    exceptionResponse.setMessage(GenericErrors.USER_VALIDATION_ERROR.getMessage());

    exceptionResponse.setDetails(errors);
    exceptionResponse.setReleaseAt(LocalDateTime.now(ZoneId.of("UTC")));
    log.error("Generic Error: {}", exceptionResponse);
    return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
    log.warn(HANDLING_EXCEPTION_MESSAGE, ex.getMessage());

    List<String> messageList = Arrays.asList(ex.getMessage());

    ErrorResponse exceptionResponse = new ErrorResponse();
    exceptionResponse.setCode(GenericErrors.USER_GENERIC_ERROR.getErrorCode());
    exceptionResponse.setMessage(GenericErrors.USER_GENERIC_ERROR.getMessage());
    exceptionResponse.setDetails(messageList);
    exceptionResponse.setReleaseAt(LocalDateTime.now(ZoneId.of("UTC")));
    log.error("Generic Error: {}", exceptionResponse);
    return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(UserException.class)
  public final ResponseEntity<Object> handleUserException(UserException ex, WebRequest request) {
    log.warn(HANDLING_EXCEPTION_MESSAGE, ex.getUserErrors().getMessage());

    ErrorResponse exceptionResponse = new ErrorResponse();

    exceptionResponse.setCode(ex.getUserErrors().getCode());
    exceptionResponse.setMessage(ex.getUserErrors().getMessage());
    exceptionResponse.setReleaseAt(LocalDateTime.now(ZoneId.of("UTC")));
    return new ResponseEntity<>(exceptionResponse, HttpStatus.UNPROCESSABLE_ENTITY);

  }

  @ExceptionHandler(EventException.class)
  public final ResponseEntity<Object> handleEventException(EventException ex, WebRequest request) {
    log.warn(HANDLING_EXCEPTION_MESSAGE, ex.getEventErrors().getMessage());

    ErrorResponse exceptionResponse = new ErrorResponse();

    exceptionResponse.setCode(ex.getEventErrors().getCode());
    exceptionResponse.setMessage(ex.getEventErrors().getMessage());
    exceptionResponse.setReleaseAt(LocalDateTime.now(ZoneId.of("UTC")));
    return new ResponseEntity<>(exceptionResponse, HttpStatus.UNPROCESSABLE_ENTITY);

  }

}
