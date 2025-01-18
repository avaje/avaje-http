package io.avaje.http.api;

import java.math.BigDecimal;
import java.time.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper type conversion methods.
 * <p/>
 * These methods are intended to be used by APT source generators.
 */
public final class PathTypeConversion {

  private PathTypeConversion() {}

  /**
   * Return the value if non-null and otherwise the default value.
   *
   * @param value        The value to return if non-null
   * @param defaultValue The default value to return
   * @return The value if non-null and otherwise the default value.
   */
  public static String withDefault(String value, String defaultValue) {
    return value != null ? value : defaultValue;
  }

  public static List<String> withDefault(List<String> value, List<String> defaultValue) {
    return value != null && !value.isEmpty() ? value : defaultValue;
  }

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

  public static <T> List<T> list(Function<String, T> func, List<String> params) {
    return params.stream().map(func).collect(Collectors.toList());
  }

  public static <T> Set<T> set(Function<String, T> func, List<String> params) {
    return params.stream().map(func).collect(Collectors.toSet());
  }

  public static <T> Optional<T> optional(Function<String, T> func, String value) {
    return Optional.ofNullable(func.apply(value));
  }

  /** Convert to int. */
  public static int asInt(String value) {
    checkNull(value);
    try {
      return Integer.parseInt(value);
    } catch (final NumberFormatException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  /** Convert to enum. */
  @SuppressWarnings({"rawtypes"})
  public static <T> Enum asEnum(Class<T> clazz, String value) {
    checkNull(value);
    try {
      return convertEnum(clazz, value);
    } catch (final IllegalArgumentException e) {
      throw new InvalidPathArgumentException(e);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> Enum convertEnum(Class<T> clazz, String value) {
    try {
      return Enum.valueOf((Class<Enum>) clazz, value);
    } catch (final IllegalArgumentException e) {
      if (value != null) {
        final String asUpper = value.toUpperCase();
        if (!asUpper.equals(value)) {
          return Enum.valueOf((Class<Enum>) clazz, asUpper);
        }
      }
      throw e;
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
      return Double.parseDouble(value);
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
      return Float.parseFloat(value);
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
   * Convert to BigInteger (not nullable).
   */
  public static BigInteger asBigInteger(String value) {
    checkNull(value);
    try {
      return new BigInteger(value);
    } catch (RuntimeException e) {
      throw new InvalidPathArgumentException(e);
    }
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

  /** Convert to enum. */
  @SuppressWarnings({"rawtypes"})
  public static <T> Enum toEnum(Class<T> clazz, String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return convertEnum(clazz, value);
    } catch (final IllegalArgumentException e) {
      throw new InvalidTypeArgumentException(e);
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
   * Convert to Double (allowing nulls).
   */
  public static Double toDouble(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return Double.valueOf(value);
    } catch (NumberFormatException e) {
      throw new InvalidTypeArgumentException(e);
    }
  }
  /**
   * Convert to Float (allowing nulls).
   */
  public static Float toFloat(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return Float.valueOf(value);
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
   * Convert to BigInteger (allowing nulls).
   */
  public static BigInteger toBigInteger(String value) {
    if (isNullOrEmpty(value)) {
      return null;
    }
    try {
      return new BigInteger(value);
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
