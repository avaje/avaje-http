package io.avaje.http.api;

import java.util.Map;

/**
 * Exception used with Validator.
 *
 * <p>Typically this is used when validating a bean populated by request body content.
 *
 * <p>Generally this exception type is registered with an exception handler and configured to return
 * a 422 or 400 http status response with the errors as a map of fields to error message.
 */
public class ValidationException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  private int status = 422;

  private Map<String, Object> errors;

  /** Create with a message. */
  public ValidationException(String message) {
    super(message);
  }

  /** Create with a status and message. */
  public ValidationException(int status, String message) {
    super(message);
    this.status = status;
  }

  /** Create with a status message and errors. */
  public ValidationException(int status, String message, Map<String, Object> errors) {
    super(message);
    this.status = status;
    this.errors = errors;
  }

  /** Create with a status message and errors. */
  public ValidationException(int status, String message, Throwable cause, Map<String, Object> errors) {
    super(message, cause);
    this.status = status;
    this.errors = errors;
  }

  /** Return the suggested HTTP status to use in the response. */
  public int getStatus() {
    return status;
  }

  /** Set the suggested HTTP status to use in the response. */
  public void setStatus(int status) {
    this.status = status;
  }

  /** Return the errors typically as a map of field to error message. */
  public Map<String, Object> getErrors() {
    return errors;
  }

  /** Set the errors. */
  public void setErrors(Map<String, Object> errors) {
    this.errors = errors;
  }
}
