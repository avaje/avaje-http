package io.avaje.http.inject;

import java.util.List;

import io.avaje.http.api.ValidationException.Violation;

public class ValidationResponse {

  private static String type = "https://avaje.io/http/#bean-validation";
  private static String title = "Failed Constraints";
  private List<Violation> errors;

  public ValidationResponse(List<Violation> errors) {
    this.errors = errors;
  }

  public String type() {
    return type;
  }

  public String title() {
    return title;
  }

  public List<Violation> errors() {
    return errors;
  }
}
