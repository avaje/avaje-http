package io.avaje.http.api;

/**
 * Exception for parameters that are required.
 * <p>
 * This is primarily intended for use when populating Kotlin form beans
 * with non-nullable properties and failing early rather than validate
 * the entire bean.
 */
public class RequiredArgumentException extends IllegalArgumentException {

  private String property;

  /**
   * Construct with a message and property.
   */
  public RequiredArgumentException(String message, String property) {
    super(message);
    this.property = property;
  }

  /**
   * Construct with an exception.
   */
  public RequiredArgumentException(Exception e) {
    super(e);
  }

  /**
   * Construct with a message and exception.
   */
  public RequiredArgumentException(String message, Exception e) {
    super(message, e);
  }

  /**
   * Return the name of the property that is required.
   */
  public String getProperty() {
    return property;
  }

  /**
   * Set the name of the required property.
   */
  public void setProperty(String property) {
    this.property = property;
  }
}
