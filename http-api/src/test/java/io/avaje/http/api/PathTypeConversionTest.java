package io.avaje.http.api;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PathTypeConversionTest {

  @Test
  void withDefault() {
    assertEquals("a", PathTypeConversion.withDefault("a", "myVal"));
    assertEquals("", PathTypeConversion.withDefault("", "myVal"));
    String nully = null;
    assertEquals("myVal", PathTypeConversion.withDefault(nully, "myVal"));
    assertThat(PathTypeConversion.withDefault(List.of(), "myVal")).anyMatch(s -> "myVal".equals(s));
  }

  @Test
  void checkNull_when_null() {
    assertThrows(RequiredArgumentException.class, () -> PathTypeConversion.checkNull(null, "id"));
  }

  @Test
  void checkNull_when_valid() {
    assertEquals("42", PathTypeConversion.checkNull("42", "id"));
  }

  @Test
  void asInt() {
    assertEquals(42, PathTypeConversion.asInt("42"));
  }

  @Test
  void asInt_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInt(null));
  }

  @Test
  void asInt_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInt("junk"));
  }

  @Test
  void asLong() {
    assertEquals(42L, PathTypeConversion.asLong("42"));
  }

  @Test
  void asLong_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLong("junk"));
  }

  @Test
  void asLong_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLong(null));
  }

  @Test
  void asDouble() {
    assertThat(PathTypeConversion.asDouble("42")).isEqualTo(42d);
  }

  @Test
  void asDouble_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asDouble("jukn"));
  }

  @Test
  void asDouble_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asDouble(null));
  }

  @Test
  void asFloat() {
    assertThat(PathTypeConversion.asFloat("42")).isEqualTo(42f);
  }

  @Test
  void asFloat_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asFloat(null));
  }

  @Test
  void asFloat_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asFloat("junk"));
  }

  @Test
  void asBool_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asBool(null));
  }

  @Test
  void asBool() {
    assertThat(PathTypeConversion.asBool("true")).isTrue();
    assertThat(PathTypeConversion.asBool("True")).isTrue();
    assertThat(PathTypeConversion.asBool("TRUE")).isTrue();
    assertThat(PathTypeConversion.asBool("false")).isFalse();
    assertThat(PathTypeConversion.asBool("42")).isFalse();
  }

  @Test
  void asBigDecimal() {
    assertThat(PathTypeConversion.asBigDecimal("42.3")).isEqualByComparingTo("42.3");
  }

  @Test
  void asBigDecimal_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asBigDecimal(null));
  }

  @Test
  void asBigDecimal_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asBigDecimal("junk"));
  }

  @Test
  void asLocalDate() {
    assertThat(PathTypeConversion.asLocalDate("2018-06-03")).isEqualTo(LocalDate.of(2018, 6, 3));
  }

  @Test
  void asLocalDate_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalDate(null));
  }

  @Test
  void asLocalDate_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalDate("junk"));
  }

  @Test
  void asLocalTime_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalTime("junk"));
  }

  @Test
  void asLocalTime_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalTime(null));
  }

  @Test
  void asLocalTime() {
    assertThat(PathTypeConversion.asLocalTime("23:34:09")).isEqualTo(LocalTime.of(23, 34, 9));
  }

  @Test
  void asInstant_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInstant(null));
  }

  @Test
  void asOffsetDateTime_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asOffsetDateTime(null));
  }

  @Test
  void asLocalDateTime_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalDateTime(null));
  }

  @Test
  void asInstant_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInstant("junk"));
  }

  @Test
  void asOffsetDateTime_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asOffsetDateTime("junk"));
  }

  @Test
  void asLocalDateTime_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asLocalDateTime("junk"));
  }

  @Test
  void asInstant() {
    Instant instant = Instant.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.asInstant(instant.toString())).isEqualTo(instant);
  }

  @Test
  void asOffsetDateTime() {
    OffsetDateTime instant = OffsetDateTime.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.asOffsetDateTime(instant.toString())).isEqualTo(instant);
  }

  @Test
  void asLocalDateTime() {
    LocalDateTime instant = LocalDateTime.parse("2018-09-23T23:34:09");
    assertThat(PathTypeConversion.asLocalDateTime(instant.toString())).isEqualTo(instant);
  }

  @Test
  void asUUID() {
    UUID uuid = UUID.randomUUID();
    assertThat(PathTypeConversion.asUUID(uuid.toString())).isEqualTo(uuid);
  }

  @Test
  void asUUID_when_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asUUID(null));
  }

  @Test
  void asUUID_when_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asUUID("junk"));
  }

  @Test
  void asInteger() {
    assertThat(PathTypeConversion.asInteger("42")).isEqualTo(42);
  }

  @Test
  void asInteger_null() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInteger(null));
  }

  @Test
  void asInteger_invalid() {
    assertThrows(InvalidPathArgumentException.class, () -> PathTypeConversion.asInteger("junk"));
  }

  @Test
  void toInteger() {
    assertThat(PathTypeConversion.toInteger("42")).isEqualTo(42);
    assertThat(PathTypeConversion.toInteger(null)).isNull();
  }

  @Test
  void toInteger_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toInteger("junk"));
  }

  @Test
  void toLong() {
    assertThat(PathTypeConversion.toLong("42")).isEqualTo(42L);
    assertThat(PathTypeConversion.toLong(null)).isNull();
  }

  @Test
  void toLong_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toLong("junk"));
  }

  @Test
  void toDouble() {
    assertThat(PathTypeConversion.toDouble("42")).isEqualTo(42D);
    assertThat(PathTypeConversion.toDouble(null)).isNull();
  }

  @Test
  void toDouble_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toDouble("junk"));
  }

  @Test
  void toFloat() {
    assertThat(PathTypeConversion.toFloat("42")).isEqualTo(42F);
    assertThat(PathTypeConversion.toFloat(null)).isNull();
  }

  @Test
  void toFloat_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toFloat("junk"));
  }

  @Test
  void toBigDecimal() {
    assertThat(PathTypeConversion.toBigDecimal("42.45")).isEqualByComparingTo("42.45");
    assertThat(PathTypeConversion.toBigDecimal(null)).isNull();
  }

  @Test
  void toBigDecimal_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toBigDecimal("junk"));
  }

  @Test
  void toBoolean() {
    assertThat(PathTypeConversion.toBoolean("true")).isTrue();
    assertThat(PathTypeConversion.toBoolean("True")).isTrue();
    assertThat(PathTypeConversion.toBoolean("false")).isFalse();
    assertThat(PathTypeConversion.toBoolean(null)).isNull();
  }

  @Test
  void toUUID() {
    UUID uuid = UUID.randomUUID();
    assertThat(PathTypeConversion.toUUID(uuid.toString())).isEqualTo(uuid);
  }

  @Test
  void toUUID_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toUUID("junk"));
  }

  @Test
  void toLocalDate() {
    assertThat(PathTypeConversion.toLocalDate("2018-09-07")).isEqualTo(LocalDate.of(2018, 9, 7));
  }

  @Test
  void toLocalDate_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toLocalDate("junk"));
  }

  @Test
  void toLocalTime() {
    assertThat(PathTypeConversion.toLocalTime("23:34:09")).isEqualTo(LocalTime.of(23, 34, 9));
  }

  @Test
  void toLocalTime_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toLocalTime("junk"));
  }

  @Test
  void toInstant() {
    Instant instant = Instant.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.toInstant("2018-09-23T23:34:09Z")).isEqualTo(instant);
  }

  @Test
  void toInstant_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toInstant("junk"));
  }

  @Test
  void toOffsetDateTime() {
    OffsetDateTime instant = OffsetDateTime.parse("2018-09-23T23:34:09Z");
    assertThat(PathTypeConversion.toOffsetDateTime("2018-09-23T23:34:09Z")).isEqualTo(instant);
  }

  @Test
  void OffsetDateTime_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toOffsetDateTime("junk"));
  }

  @Test
  void toLocalDateTime() {
    LocalDateTime instant = LocalDateTime.parse("2018-09-23T23:34:09");
    assertThat(PathTypeConversion.toLocalDateTime("2018-09-23T23:34:09")).isEqualTo(instant);
  }

  @Test
  void LocalDateTime_invalid() {
    assertThrows(InvalidTypeArgumentException.class, () -> PathTypeConversion.toLocalDateTime("junk"));
  }
}
