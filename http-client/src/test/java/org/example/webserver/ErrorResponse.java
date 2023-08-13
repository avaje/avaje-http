package org.example.webserver;

import io.avaje.http.api.ValidationException;

import java.util.*;

public class ErrorResponse {

  private String message;

  private List<ValidationException.Violation> errors = new ArrayList<>();

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public List<ValidationException.Violation> getErrors() {
    return errors;
  }

  public void setErrors(List<ValidationException.Violation> errors) {
    this.errors = errors;
  }

  public String get(String field) {
    return errorForField(field)
      .map(ValidationException.Violation::message)
      .orElseThrow();
  }
  public Optional<ValidationException.Violation> errorForField(String field) {
    for (ValidationException.Violation error : errors) {
      if (field.equals(error.field())) {
        return Optional.of(error);
      }
    }
    return Optional.empty();
  }
}
