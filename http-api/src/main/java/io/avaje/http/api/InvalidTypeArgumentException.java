package io.avaje.http.api;

/**
 * Exception for all invalid path type conversions - numbers, uuid, date time types etc.
 */
public class InvalidTypeArgumentException extends IllegalArgumentException {

  public InvalidTypeArgumentException(String message) {
    super(message);
  }

  public InvalidTypeArgumentException(Exception e) {
    super(e);
  }

  public InvalidTypeArgumentException(String message, Exception e) {
    super(message, e);
  }

}
