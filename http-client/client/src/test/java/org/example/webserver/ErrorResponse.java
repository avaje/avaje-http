package org.example.webserver;

import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorResponse {

  private String message;

  private Map<String,String> errors = new LinkedHashMap<>();

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Map<String, String> getErrors() {
    return errors;
  }

  public void setErrors(Map<String, String> errors) {
    this.errors = errors;
  }
}
