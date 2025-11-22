package io.avaje.http.inject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import io.avaje.http.api.ValidationException.Violation;

public class ValidationResponse {

  private static String type = "tag:io.avaje.http.api.ValidationException";
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
  
  // custom serialize as this is a simple class
  public void toJson(OutputStream os) throws IOException {
    try (Writer writer = new OutputStreamWriter(os, "UTF-8")) {
      writeJsonInternal(writer);
    }
  }

  private void writeJsonInternal(Writer writer) throws IOException {
    writer.write('{');
    writeKeyValue("type", type, writer);
    writer.write(',');
    writeKeyValue("title", title, writer);
    writer.write(',');
    writeKeyValue("detail", detail, writer);
    writer.write(',');
    writeKeyValue("instance", instance, writer);
    writer.write(',');
    // status is a number, so no quotes or escaping needed
    writer.write("\"status\":");
    writer.write(String.valueOf(status));
    writer.write(",\"errors\":[");
    for (int i = 0; i < errors.size(); i++) {
      if (i > 0) {
        writer.write(',');
      }
      var e = errors.get(i);
      writer.write('{');
      writeKeyValue("path", e.getPath(), writer);
      writer.write(',');
      writeKeyValue("field", e.getField(), writer);
      writer.write(',');
      writeKeyValue("message", e.getMessage(), writer);
      writer.write('}');
    }

    writer.write(']');
    writer.write('}');
    writer.flush();
  }

  /** Writes a JSON key-value pair where the value is a string, handling quotes and escaping. */
  private void writeKeyValue(String key, String value, Writer writer) throws IOException {
    writer.write('"');
    writer.write(key);
    writer.write("\":");
    writeEscapedJsonString(value, writer);
  }

  /**
   * Writes the given string to the writer, JSON-escaping it and wrapping it in quotes. Writes
   * 'null' (the JSON literal) if the input string is null.
   */
  private static void writeEscapedJsonString(String s, Writer writer) throws IOException {
    if (s == null) {
      writer.write("null");
      return;
    }

    writer.write('"');
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      switch (ch) {
        case '"':
          writer.write("\\\"");
          break;
        case '\\':
          writer.write("\\\\");
          break;
        case '\b':
          writer.write("\\b");
          break;
        case '\f':
          writer.write("\\f");
          break;
        case '\n':
          writer.write("\\n");
          break;
        case '\r':
          writer.write("\\r");
          break;
        case '\t':
          writer.write("\\t");
          break;
        default:
          // Check for control characters that must be escaped
          if (ch < ' ' || ch >= 0x7F && ch <= 0x9F) {
            writer.write(String.format("\\u%04x", (int) ch));
          } else {
            writer.write(ch);
          }
      }
    }
    writer.write('"');
  }
}
