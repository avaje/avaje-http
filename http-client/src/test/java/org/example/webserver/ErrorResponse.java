package org.example.webserver;

import io.avaje.http.api.ValidationException;

import java.util.*;

public class ErrorResponse {

  private String message;

  private List<ValidationException.ViolationMessage> errors = new ArrayList<>();

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public List<ValidationException.ViolationMessage> getErrors() {
    return errors;
  }

  public void setErrors(List<ValidationException.ViolationMessage> errors) {
    this.errors = errors;
  }

  public String get(String field) {
    return errorForField(field)
      .map(ValidationException.ViolationMessage::getMessage)
      .orElseThrow();
  }
  public Optional<ValidationException.ViolationMessage> errorForField(String field) {
    for (ValidationException.ViolationMessage error : errors) {
      if (field.equals(error.getField())) {
        return Optional.of(error);
      }
    }
    return Optional.empty();
  }
}
