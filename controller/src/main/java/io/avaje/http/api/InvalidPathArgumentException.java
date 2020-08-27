package io.avaje.http.api;

/**
 * Exception for all invalid path type conversions - numbers, uuid, date time types etc.
 */
public class InvalidPathArgumentException extends InvalidTypeArgumentException {

  public InvalidPathArgumentException(String message) {
    super(message);
  }

  public InvalidPathArgumentException(Exception e) {
    super(e);
  }

  public InvalidPathArgumentException(String message, Exception e) {
    super(message, e);
  }

}
