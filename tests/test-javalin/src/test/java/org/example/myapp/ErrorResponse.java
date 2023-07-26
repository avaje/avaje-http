package org.example.myapp;

import io.avaje.http.api.ValidationException;

import java.util.*;

public class ErrorResponse {

  private String message;

  private List<ValidationException.Error> errors = new ArrayList<>();

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public List<ValidationException.Error> getErrors() {
    return errors;
  }

  public void setErrors(List<ValidationException.Error> errors) {
    this.errors = errors;
  }

  public String get(String field) {
    return errorForField(field)
      .map(ValidationException.Error::getMessage)
      .orElseThrow();
  }
  public Optional<ValidationException.Error> errorForField(String field) {
    for (ValidationException.Error error : errors) {
      if (field.equals(error.getField())) {
        return Optional.of(error);
      }
    }
    return Optional.empty();
  }
}
