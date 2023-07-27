package io.avaje.http.api;

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

  private int status = 422;

  private List<? extends Violation> errors;

  /** Create with a message. */
  public ValidationException(String message) {
    super(message);
    this.errors = new ArrayList<>();
  }

  /** Create with a status and message. */
  public ValidationException(int status, String message) {
    super(message);
    this.status = status;
    this.errors = new ArrayList<>();
  }

  /** Create with a status message and errors. */
  public ValidationException(int status, String message, List<? extends Violation> errors) {
    super(message);
    this.status = status;
    this.errors = errors;
  }

  /** Create with a status message and errors. */
  public ValidationException(int status, String message, Throwable cause, List<? extends Violation> errors) {
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
  public List<? extends Violation> getErrors() {
    return errors;
  }

  /** Set the errors. */
  public void setErrors(List<? extends Violation> errors) {
    this.errors = errors;
  }

  /** Error details including the field, error message and path */
  public static class Violation {

    protected String path;
    protected String field;
    protected String message;

    public Violation(String path, String field, String message) {
      this.path = path;
      this.field = field;
      this.message = message;
    }

    public Violation() {
    }

    /** Return the path of this error message. */
    public String getPath() {
      return path;
    }

    /** Return the field for this error message. */
    public String getField() {
      return field;
    }

    /** Return the error message. */
    public String getMessage() {
      return message;
    }

    /** Set the path for this error. */
    public void setPath(String path) {
      this.path = path;
    }

    /** Set the field for this error. */
    public void setField(String field) {
      this.field = field;
    }

    /** Set the error message. */
    public void setMessage(String message) {
      this.message = message;
    }
  }
}
