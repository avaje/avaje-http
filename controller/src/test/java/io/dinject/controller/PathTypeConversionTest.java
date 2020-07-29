package io.dinject.controller;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;

public class PathTypeConversionTest {

  @Test(expected = RequiredArgumentException.class)
  public void checkNull_when_null() {
    PathTypeConversion.checkNull(null, "id");
  }

  @Test
  public void checkNull_when_valid() {
    assertEquals("42", PathTypeConversion.checkNull("42", "id"));
  }

  @Test
  public void asInt() {
    assertEquals(42, PathTypeConversion.asInt("42"));
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asInt_when_null() {
    PathTypeConversion.asInt(null);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asInt_when_invalid() {
    PathTypeConversion.asInt("junk");
  }

  @Test
  public void asLong() {
    assertEquals(42L, PathTypeConversion.asInt("42"));
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asLong_when_invalid() {
    PathTypeConversion.asLong("junk");
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asLong_when_null() {
    PathTypeConversion.asLong(null);
  }

  @Test
  public void asDouble() {
    assertThat(PathTypeConversion.asDouble("42")).isEqualTo(42d);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asDouble_when_invalid() {
    PathTypeConversion.asDouble("jukn");
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asDouble_when_null() {
    PathTypeConversion.asDouble(null);
  }

  @Test
  public void asFloat() {
    assertThat(PathTypeConversion.asFloat("42")).isEqualTo(42f);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asFloat_when_null() {
    PathTypeConversion.asFloat(null);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asFloat_when_invalid() {
    PathTypeConversion.asFloat("junk");
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asBool_when_null() {
    PathTypeConversion.asBool(null);
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

  @Test(expected = InvalidPathArgumentException.class)
  public void asBigDecimal_when_null() {
    PathTypeConversion.asBigDecimal(null);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asBigDecimal_when_invalid() {
    PathTypeConversion.asBigDecimal("junk");
  }

  @Test
  public void asLocalDate() {
    assertThat(PathTypeConversion.asLocalDate("2018-06-03")).isEqualTo(LocalDate.of(2018, 6, 3));
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asLocalDate_when_null() {
    PathTypeConversion.asLocalDate(null);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asLocalDate_when_invalid() {
    PathTypeConversion.asLocalDate("junk");
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asLocalTime_when_invalid() {
    PathTypeConversion.asLocalTime("junk");
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asLocalTime_when_null() {
    PathTypeConversion.asLocalTime(null);
  }

  @Test
  public void asLocalTime() {
    assertThat(PathTypeConversion.asLocalTime("23:34:09")).isEqualTo(LocalTime.of(23, 34, 9));
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asInstant_when_null() {
    PathTypeConversion.asInstant(null);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asOffsetDateTime_when_null() {
    PathTypeConversion.asOffsetDateTime(null);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asLocalDateTime_when_null() {
    PathTypeConversion.asLocalDateTime(null);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asInstant_when_invalid() {
    PathTypeConversion.asInstant("junk");
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asOffsetDateTime_when_invalid() {
    PathTypeConversion.asOffsetDateTime("junk");
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asLocalDateTime_when_invalid() {
    PathTypeConversion.asLocalDateTime("junk");
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

  @Test(expected = InvalidPathArgumentException.class)
  public void asUUID_when_null() {
    PathTypeConversion.asUUID(null);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asUUID_when_invalid() {
    PathTypeConversion.asUUID("junk");
  }

  @Test
  public void asInteger() {
    assertThat(PathTypeConversion.asInteger("42")).isEqualTo(42);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asInteger_null() {
    PathTypeConversion.asInteger(null);
  }

  @Test(expected = InvalidPathArgumentException.class)
  public void asInteger_invalid() {
    PathTypeConversion.asInteger("junk");
  }

  @Test
  public void toInteger() {
    assertThat(PathTypeConversion.toInteger("42")).isEqualTo(42);
    assertThat(PathTypeConversion.toInteger(null)).isNull();
  }

  @Test(expected = InvalidTypeArgumentException.class)
  public void toInteger_invalid() {
    PathTypeConversion.toInteger("junk");
  }

  @Test
  public void toLong() {
    assertThat(PathTypeConversion.toLong("42")).isEqualTo(42L);
    assertThat(PathTypeConversion.toLong(null)).isNull();
  }

  @Test(expected = InvalidTypeArgumentException.class)
  public void toLong_invalid() {
    PathTypeConversion.toLong("junk");
  }

  @Test
  public void toBigDecimal() {
    assertThat(PathTypeConversion.toBigDecimal("42.45")).isEqualByComparingTo("42.45");
    assertThat(PathTypeConversion.toBigDecimal(null)).isNull();
  }

  @Test(expected = InvalidTypeArgumentException.class)
  public void toBigDecimal_invalid() {
    PathTypeConversion.toBigDecimal("junk");
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

  @Test(expected = InvalidTypeArgumentException.class)
  public void toUUID_invalid() {
    PathTypeConversion.toUUID("junk");
  }

  @Test
  public void toLocalDate() {
    assertThat(PathTypeConversion.toLocalDate("2018-09-07")).isEqualTo(LocalDate.of(2018, 9, 7));
  }

  @Test(expected = InvalidTypeArgumentException.class)
  public void toLocalDate_invalid() {
    PathTypeConversion.toLocalDate("junk");
  }

  @Test
  public void toLocalTime() {
    assertThat(PathTypeConversion.toLocalTime("23:34:09")).isEqualTo(LocalTime.of(23, 34, 9));
  }

  @Test(expected = InvalidTypeArgumentException.class)
  public void toLocalTime_invalid() {
    PathTypeConversion.toLocalTime("junk");
  }

  @Test
  public void toInstant() {
    Instant instant = Instant.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.toInstant("2018-09-23T23:34:09Z")).isEqualTo(instant);
  }

  @Test(expected = InvalidTypeArgumentException.class)
  public void toInstant_invalid() {
    PathTypeConversion.toInstant("junk");
  }

  @Test
  public void toOffsetDateTime() {
    OffsetDateTime instant = OffsetDateTime.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.toOffsetDateTime("2018-09-23T23:34:09Z")).isEqualTo(instant);
  }

  @Test(expected = InvalidTypeArgumentException.class)
  public void OffsetDateTime_invalid() {
    PathTypeConversion.toOffsetDateTime("junk");
  }

  @Test
  public void toLocalDateTime() {
    LocalDateTime instant = LocalDateTime.parse("2018-09-23T23:34:09");
    assertThat(PathTypeConversion.toLocalDateTime("2018-09-23T23:34:09")).isEqualTo(instant);
  }

  @Test(expected = InvalidTypeArgumentException.class)
  public void LocalDateTime_invalid() {
    PathTypeConversion.toLocalDateTime("junk");
  }
}
