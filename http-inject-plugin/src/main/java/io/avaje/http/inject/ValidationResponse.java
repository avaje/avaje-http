package io.avaje.http.inject;

import java.util.List;

import io.avaje.http.api.ValidationException.Violation;

public class ValidationResponse {

  private static String type = "tag:io.avaje.http.api.Validator";
  private static String title = "Request Failed Validation";
  private static String detail = "You tried to call this endpoint, but your data failed validation";
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

  // Custom serialize so we don't need any json lib
  public String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    sb.append("\"type\":").append(escapeJson(type)).append(",");
    sb.append("\"title\":").append(escapeJson(title)).append(",");
    sb.append("\"detail\":").append(escapeJson(detail)).append(",");
    sb.append("\"instance\":").append(escapeJson(instance)).append(",");
    sb.append("\"status\":").append(status).append(',');

    sb.append("\"errors\":[");
    for (int i = 0; i < errors().size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      var e = errors.get(i);
      sb.append('{');
      sb.append("\"path\":").append(escapeJson(e.getPath())).append(",");
      sb.append("\"field\":").append(escapeJson(e.getField())).append(",");
      sb.append("\"message\":").append(escapeJson(e.getMessage()));
      sb.append('}');
    }

    sb.append(']');

    sb.append('}');
    return sb.toString();
  }

  private static String escapeJson(String s) {
    if (s == null) {
      return "null";
    }
    StringBuilder sb = new StringBuilder();
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      switch (ch) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          // Handle control characters or just append
          if (ch < ' ' || ch > '~') {
            sb.append(String.format("\\u%04x", (int) ch));
          } else {
            sb.append(ch);
          }
      }
    }
    sb.append('"');
    return sb.toString();
  }
}
