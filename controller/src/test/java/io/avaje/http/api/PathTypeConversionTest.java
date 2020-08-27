package io.avaje.http.api;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PathTypeConversionTest {

  @Test
  public void checkNull_when_null() {
    assertThrows(RequiredArgumentException.class, () -> PathTypeConversion.checkNull(null, "id"));
  }

  @Test
  public void checkNull_when_valid() {
    assertEquals("42", PathTypeConversion.checkNull("42", "id"));
  }

  @Test
  public void asInt() {
    assertEquals(42, PathTypeConversion.asInt("42"));
  }

  @Test
  public void asInt_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInt(null));
  }

  @Test
  public void asInt_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInt("junk"));
  }

  @Test
  public void asLong() {
    assertEquals(42L, PathTypeConversion.asInt("42"));
  }

  @Test
  public void asLong_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLong("junk"));
  }

  @Test
  public void asLong_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLong(null));
  }

  @Test
  public void asDouble() {
    assertThat(PathTypeConversion.asDouble("42")).isEqualTo(42d);
  }

  @Test
  public void asDouble_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asDouble("jukn"));
  }

  @Test
  public void asDouble_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asDouble(null));
  }

  @Test
  public void asFloat() {
    assertThat(PathTypeConversion.asFloat("42")).isEqualTo(42f);
  }

  @Test
  public void asFloat_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asFloat(null));
  }

  @Test
  public void asFloat_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asFloat("junk"));
  }

  @Test
  public void asBool_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asBool(null));
  }

  @Test
  public void asBool() {
    assertThat(PathTypeConversion.asBool("true")).isTrue();
    assertThat(PathTypeConversion.asBool("True")).isTrue();
    assertThat(PathTypeConversion.asBool("TRUE")).isTrue();
    assertThat(PathTypeConversion.asBool("false")).isFalse();
    assertThat(PathTypeConversion.asBool("42")).isFalse();
  }

  @Test
  public void asBigDecimal() {
    assertThat(PathTypeConversion.asBigDecimal("42.3")).isEqualByComparingTo("42.3");
  }

  @Test
  public void asBigDecimal_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asBigDecimal(null));
  }

  @Test
  public void asBigDecimal_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asBigDecimal("junk"));
  }

  @Test
  public void asLocalDate() {
    assertThat(PathTypeConversion.asLocalDate("2018-06-03")).isEqualTo(LocalDate.of(2018, 6, 3));
  }

  @Test
  public void asLocalDate_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalDate(null));
  }

  @Test
  public void asLocalDate_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalDate("junk"));
  }

  @Test
  public void asLocalTime_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalTime("junk"));
  }

  @Test
  public void asLocalTime_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalTime(null));
  }

  @Test
  public void asLocalTime() {
    assertThat(PathTypeConversion.asLocalTime("23:34:09")).isEqualTo(LocalTime.of(23, 34, 9));
  }

  @Test
  public void asInstant_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInstant(null));
  }

  @Test
  public void asOffsetDateTime_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asOffsetDateTime(null));
  }

  @Test
  public void asLocalDateTime_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalDateTime(null));
  }

  @Test
  public void asInstant_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInstant("junk"));
  }

  @Test
  public void asOffsetDateTime_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asOffsetDateTime("junk"));
  }

  @Test
  public void asLocalDateTime_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalDateTime("junk"));
  }

  @Test
  public void asInstant() {
    Instant instant = Instant.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.asInstant(instant.toString())).isEqualTo(instant);
  }

  @Test
  public void asOffsetDateTime() {
    OffsetDateTime instant = OffsetDateTime.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.asOffsetDateTime(instant.toString())).isEqualTo(instant);
  }

  @Test
  public void asLocalDateTime() {
    LocalDateTime instant = LocalDateTime.parse("2018-09-23T23:34:09");
    assertThat(PathTypeConversion.asLocalDateTime(instant.toString())).isEqualTo(instant);
  }

  @Test
  public void asUUID() {
    UUID uuid = UUID.randomUUID();
    assertThat(PathTypeConversion.asUUID(uuid.toString())).isEqualTo(uuid);
  }

  @Test
  public void asUUID_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asUUID(null));
  }

  @Test
  public void asUUID_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asUUID("junk"));
  }

  @Test
  public void asInteger() {
    assertThat(PathTypeConversion.asInteger("42")).isEqualTo(42);
  }

  @Test
  public void asInteger_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInteger(null));
  }

  @Test
  public void asInteger_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInteger("junk"));
  }

  @Test
  public void toInteger() {
    assertThat(PathTypeConversion.toInteger("42")).isEqualTo(42);
    assertThat(PathTypeConversion.toInteger(null)).isNull();
  }

  @Test
  public void toInteger_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toInteger("junk"));
  }

  @Test
  public void toLong() {
    assertThat(PathTypeConversion.toLong("42")).isEqualTo(42L);
    assertThat(PathTypeConversion.toLong(null)).isNull();
  }

  @Test
  public void toLong_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toLong("junk"));
  }

  @Test
  public void toBigDecimal() {
    assertThat(PathTypeConversion.toBigDecimal("42.45")).isEqualByComparingTo("42.45");
    assertThat(PathTypeConversion.toBigDecimal(null)).isNull();
  }

  @Test
  public void toBigDecimal_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toBigDecimal("junk"));
  }

  @Test
  public void toBoolean() {
    assertThat(PathTypeConversion.toBoolean("true")).isTrue();
    assertThat(PathTypeConversion.toBoolean("True")).isTrue();
    assertThat(PathTypeConversion.toBoolean("false")).isFalse();
    assertThat(PathTypeConversion.toBoolean(null)).isNull();
  }

  @Test
  public void toUUID() {
    UUID uuid = UUID.randomUUID();
    assertThat(PathTypeConversion.toUUID(uuid.toString())).isEqualTo(uuid);
  }

  @Test
  public void toUUID_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toUUID("junk"));
  }

  @Test
  public void toLocalDate() {
    assertThat(PathTypeConversion.toLocalDate("2018-09-07")).isEqualTo(LocalDate.of(2018, 9, 7));
  }

  @Test
  public void toLocalDate_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toLocalDate("junk"));
  }

  @Test
  public void toLocalTime() {
    assertThat(PathTypeConversion.toLocalTime("23:34:09")).isEqualTo(LocalTime.of(23, 34, 9));
  }

  @Test
  public void toLocalTime_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toLocalTime("junk"));
  }

  @Test
  public void toInstant() {
    Instant instant = Instant.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.toInstant("2018-09-23T23:34:09Z")).isEqualTo(instant);
  }

  @Test
  public void toInstant_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toInstant("junk"));
  }

  @Test
  public void toOffsetDateTime() {
    OffsetDateTime instant = OffsetDateTime.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.toOffsetDateTime("2018-09-23T23:34:09Z")).isEqualTo(instant);
  }

  @Test
  public void OffsetDateTime_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toOffsetDateTime("junk"));
  }

  @Test
  public void toLocalDateTime() {
    LocalDateTime instant = LocalDateTime.parse("2018-09-23T23:34:09");
    assertThat(PathTypeConversion.toLocalDateTime("2018-09-23T23:34:09")).isEqualTo(instant);
  }

  @Test
  public void LocalDateTime_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toLocalDateTime("junk"));
  }
}
