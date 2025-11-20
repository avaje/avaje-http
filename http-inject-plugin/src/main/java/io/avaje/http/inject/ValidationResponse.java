package io.avaje.http.inject;

import java.util.List;

import io.avaje.http.api.ValidationException.Violation;

public class ValidationResponse {

  private static String type = "tag:io.avaje.http.api.Validator";
  private static String title = "Request Failed Validation";
  private static String detail =
      "You tried to call this endpoint, but your data failed validation";
  private final int status;
  private final List<Violation> errors;
  private final String instance;

  public ValidationResponse(int status, List<Violation> errors, String instance) {
    this.status = status;
    this.errors = errors;
    this.instance = instance;
  }

  public String type() {
    return type;
  }

  public String title() {
    return title;
  }

  public String detail() {
    return detail;
  }

  public String instance() {
    return instance;
  }

  public int status() {
    return status;
  }

  public List<Violation> errors() {
    return errors;
  }
}
