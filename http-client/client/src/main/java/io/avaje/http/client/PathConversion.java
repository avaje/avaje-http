package io.avaje.http.client;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

/**
 * Helper methods to convert common types to String path values.
 */
public class PathConversion {

  /**
   * Convert to path.
   */
  public static String toPath(int val) {
    return Integer.toString(val);
  }

  /**
   * Convert to path.
   */
  public static String toPath(long val) {
    return Long.toString(val);
  }

  /**
   * Convert to path.
   */
  public static String toPath(boolean val) {
    return Boolean.toString(val);
  }

  /**
   * Convert to path.
   */
  public static String toPath(UUID val) {
    return val.toString();
  }

  /**
   * Convert to path.
   */
  public static String toPath(LocalDate val) {
    return val.toString();
  }

  /**
   * Convert to path.
   */
  public static String toPath(LocalTime val) {
    return val.toString();
  }

  /**
   * Convert to path.
   */
  public static String toPath(LocalDateTime val) {
    return val.toString();
  }

  /**
   * Convert to path.
   */
  public static String toPath(Instant val) {
    return val.toString();
  }

  /**
   * Convert to path.
   */
  public static String toPath(OffsetDateTime val) {
    return val.toString();
  }

  /**
   * Convert to path.
   */
  public static String toPath(ZonedDateTime val) {
    return val.toString();
  }

  /**
   * Convert to path.
   */
  public static String toPath(ZoneId val) {
    return val.toString();
  }

  /**
   * Convert to path.
   */
  public static String toPath(BigDecimal val) {
    return val.toString();
  }

  /**
   * Convert to path.
   */
  public static String toPath(double val) {
    return Double.toString(val);
  }

  /**
   * Convert to path.
   */
  public static String toPath(float val) {
    return Float.toString(val);
  }

}
