package io.dinject.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Helper type conversion methods.
 * <p/>
 * These methods are intended to be used by APT source generators.
 */
public class PathTypeConversion {

  /**
   * Check for null for a required property throwing RequiredArgumentException
   * if the value is null.
   *
   * @return The value being checked
   */
  public static String checkNull(String value, String property) {
    if (value == null) {
      throw new RequiredArgumentException("Required property " + property + " was not supplied.", property);
    }
    return value;
  }

  private static void checkNull(String value) {
    if (value == null) {
      throw new InvalidPathArgumentException("path element is null");
    }
  }

  /**
   * Convert to int.
   */
  public static int asInt(String value) {
    checkNull(value);
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to long.
   */
  public static long asLong(String value) {
    checkNull(value);
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to double.
   */
  public static double asDouble(String value) {
    checkNull(value);
    try {
      return Double.valueOf(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to float.
   */
  public static float asFloat(String value) {
    checkNull(value);
    try {
      return Float.valueOf(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to boolean.
   */
  public static boolean asBoolean(String value) {
    checkNull(value);
    return Boolean.parseBoolean(value);
  }

  /**
   * Convert to boolean.
   */
  public static boolean asBool(String value) {
    return asBoolean(value);
  }

  /**
   * Convert to BigDecimal (not nullable).
   */
  public static BigDecimal asBigDecimal(String value) {
    checkNull(value);
    try {
      return new BigDecimal(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to LocalDate (not nullable).
   */
  public static LocalDate asLocalDate(String value) {
    checkNull(value);
    try {
      return LocalDate.parse(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to LocalTime (not nullable).
   */
  public static LocalTime asLocalTime(String value) {
    checkNull(value);
    try {
      return LocalTime.parse(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to Instant (not nullable).
   */
  public static Instant asInstant(String value) {
    checkNull(value);
    try {
      return Instant.parse(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to OffsetDateTime (not nullable).
   */
  public static OffsetDateTime asOffsetDateTime(String value) {
    checkNull(value);
    try {
      return OffsetDateTime.parse(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to LocalDateTime (not nullable).
   */
  public static LocalDateTime asLocalDateTime(String value) {
    checkNull(value);
    try {
      return LocalDateTime.parse(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to UUID (not nullable).
   */
  public static UUID asUUID(String value) {
    checkNull(value);
    try {
      return UUID.fromString(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to Integer (not nullable).
   */
  public static Integer asInteger(String value) {
    checkNull(value);
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /**
   * Convert to Integer (allowing nulls).
   */
  public static Integer toInteger(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException e) {
      throw new InvalidTypeArgumentException(e);
    }
  }

  /**
   * Convert to Long (allowing nulls).
   */
  public static Long toLong(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return Long.valueOf(value);
    } catch (NumberFormatException e) {
      throw new InvalidTypeArgumentException(e);
    }
  }

  /**
   * Convert to BigDecimal (allowing nulls).
   */
  public static BigDecimal toBigDecimal(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return new BigDecimal(value);
    } catch (Exception e) {
      throw new InvalidTypeArgumentException(e);
    }
  }

  /**
   * Convert to Boolean (allowing nulls).
   */
  public static Boolean toBoolean(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    return Boolean.valueOf(value);
  }

  /**
   * Convert to UUID (allowing nulls).
   */
  public static UUID toUUID(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return UUID.fromString(value);
    } catch (Exception e) {
      throw new InvalidTypeArgumentException(e);
    }
  }

  /**
   * Convert to LocalDate (allowing nulls).
   */
  public static LocalDate toLocalDate(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return LocalDate.parse(value);
    } catch (Exception e) {
      throw new InvalidTypeArgumentException(e);
    }
  }

  /**
   * Convert to LocalTime (allowing nulls).
   */
  public static LocalTime toLocalTime(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return LocalTime.parse(value);
    } catch (Exception e) {
      throw new InvalidTypeArgumentException(e);
    }
  }

  /**
   * Convert to Instant (allowing nulls).
   */
  public static Instant toInstant(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return Instant.parse(value);
    } catch (Exception e) {
      throw new InvalidTypeArgumentException(e);
    }
  }

  /**
   * Convert to OffsetDateTime (allowing nulls).
   */
  public static OffsetDateTime toOffsetDateTime(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return OffsetDateTime.parse(value);
    } catch (Exception e) {
      throw new InvalidTypeArgumentException(e);
    }
  }

  /**
   * Convert to LocalDateTime (allowing nulls).
   */
  public static LocalDateTime toLocalDateTime(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return LocalDateTime.parse(value);
    } catch (Exception e) {
      throw new InvalidTypeArgumentException(e);
    }
  }

  private static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }

}
