package io.avaje.http.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Exception used with Validator.
 *
 * <p>Typically this is used when validating a bean populated by request body content.
 *
 * <p>Generally this exception type is registered with an exception handler and configured to return
 * a 422 or 400 http status response with the errors as a list of field error message.
 */
public class ValidationException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  private final int status;

  private final List<Violation> errors;

  /** Create with a message. */
  public ValidationException(String message) {
    super(message);
    this.errors = new ArrayList<>();
    this.status = 422;
  }

  /** Create with a status and message. */
  public ValidationException(int status, String message) {
    super(message);
    this.status = status;
    this.errors = new ArrayList<>();
  }

  /** Create with a status message and errors. */
  public ValidationException(int status, String message, List<Violation> errors) {
    super(message);
    this.status = status;
    this.errors = errors;
  }

  /** Create with a status message and errors. */
  public ValidationException(int status, String message, Throwable cause, List<Violation> errors) {
    super(message, cause);
    this.status = status;
    this.errors = errors;
  }

  /** Return the suggested HTTP status to use in the response. */
  public int getStatus() {
    return status;
  }

  /** Return the errors typically as a map of field to error message. */
  public List<Violation> getErrors() {
    return errors;
  }

  /** Error details including the field, error message and path */
  public static class Violation implements Serializable {

    private static final long serialVersionUID = 1;

    protected final String path;
    protected final String field;
    protected final String message;

    /** Create with path, field and message */
    public Violation(String path, String field, String message) {
      this.path = path;
      this.field = field;
      this.message = message;
    }

    /** Return the path of this error message. */
    public String path() {
      return path;
    }

    /** Return the field for this error message. */
    public String field() {
      return field;
    }

    /** Return the error message. */
    public String message() {
      return message;
    }
  }
}
