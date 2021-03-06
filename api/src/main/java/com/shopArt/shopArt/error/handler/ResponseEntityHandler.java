package com.shopArt.shopArt.error.handler;

import com.shopArt.shopArt.error.UserAlreadyExistException;
import com.shopArt.shopArt.util.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class ResponseEntityHandler extends ResponseEntityExceptionHandler {
  
  @Autowired
  private MessageSource messages;
  
  @Override
  protected ResponseEntity<Object> handleBindException
    (BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    logger.error("400 Status Code", ex);
    BindingResult result = ex.getBindingResult();
    GenericResponse bodyOfResponse =
      new GenericResponse(result.getFieldErrors(), result.getGlobalErrors());

    return handleExceptionInternal(
      ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler({ UserAlreadyExistException.class })
  public ResponseEntity<Object> handleUserAlreadyExist(RuntimeException ex, WebRequest request) {
    logger.error("409 Status Code", ex);
    GenericResponse bodyOfResponse = new GenericResponse(messages.getMessage("message.regError", null, request.getLocale()), "UserAlreadyExist");

    return handleExceptionInternal(
      ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, request);
  }


  @ExceptionHandler({ RepositoryConstraintViolationException.class })
  public ResponseEntity<Object> handleAccessDeniedException(
    Exception ex, WebRequest request) {
    RepositoryConstraintViolationException nevEx =
      (RepositoryConstraintViolationException) ex;

    String errors = nevEx.getErrors().getAllErrors().stream()
      .map(p -> p.toString()).collect(Collectors.joining("\n"));

    return new ResponseEntity<Object>(errors, new HttpHeaders(),
      HttpStatus.PARTIAL_CONTENT);
  }

}
