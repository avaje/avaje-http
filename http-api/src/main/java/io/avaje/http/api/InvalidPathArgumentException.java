package io.avaje.http.api;

/**
 * Exception for all invalid path type conversions - numbers, uuid, date time types etc.
 */
public class InvalidPathArgumentException extends InvalidTypeArgumentException {

  private static final long serialVersionUID = 1;

  /**
   * Construct with a message.
   */
  public InvalidPathArgumentException(String message) {
    super(message);
  }

  /**
   * Construct with an exception.
   */
  public InvalidPathArgumentException(Exception e) {
    super(e);
  }

  /**
   * Construct with message and exception.
   */
  public InvalidPathArgumentException(String message, Exception e) {
    super(message, e);
  }

}
