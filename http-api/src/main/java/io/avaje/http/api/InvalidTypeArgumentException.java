package io.avaje.http.api;

/**
 * Exception for all invalid path type conversions - numbers, uuid, date time types etc.
 */
public class InvalidTypeArgumentException extends IllegalArgumentException {

  private static final long serialVersionUID = 1;

  /**
   * Construct with a message.
   */
  public InvalidTypeArgumentException(String message) {
    super(message);
  }

  /**
   * Construct with an exception.
   */
  public InvalidTypeArgumentException(Exception e) {
    super(e);
  }

  /**
   * Construct with a message and exception.
   */
  public InvalidTypeArgumentException(String message, Exception e) {
    super(message, e);
  }

}
