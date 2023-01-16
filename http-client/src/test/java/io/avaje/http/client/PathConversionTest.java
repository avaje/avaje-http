package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PathConversionTest {

  @Test
  void toPath() {
    assertEquals("1.3", PathConversion.toPath(1.3d));
    assertEquals("1.3", PathConversion.toPath(1.3f));
    assertEquals("42", PathConversion.toPath(42));
    assertEquals("87", PathConversion.toPath(87L));
    assertEquals("true", PathConversion.toPath(true));
    assertEquals("false", PathConversion.toPath(false));
  }

  @Test
  void toPath_BigDecimal() {
    BigDecimal val = new BigDecimal("42.567");
    assertEquals("42.567", PathConversion.toPath(val));
  }

  @Test
  void toPath_date() {
    LocalDate date = LocalDate.of(2020, 2, 5);
    assertEquals("2020-02-05", PathConversion.toPath(date));
  }

  @Test
  void toPath_uuid() {
    UUID uuid = UUID.randomUUID();
    assertEquals(uuid.toString(), PathConversion.toPath(uuid));
  }

  @Test
  void toPath_zoneId() {
    ZoneId zoneId = ZoneId.systemDefault();
    assertEquals(zoneId.toString(), PathConversion.toPath(zoneId));
  }

  @Test
  void toPath_OffsetDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    assertEquals(now.toString(), PathConversion.toPath(now));
  }

  @Test
  void toPath_ZonedDateTime() {
    ZonedDateTime now = ZonedDateTime.now();
    assertEquals(now.toString(), PathConversion.toPath(now));
  }

  @Test
  void toPath_Instant() {
    Instant now = Instant.now();
    assertEquals(now.toString(), PathConversion.toPath(now));
  }

  @Test
  void toPath_LocalTime() {
    LocalTime val = LocalTime.of(13, 43);
    assertEquals("13:43", PathConversion.toPath(val));
  }

  @Test
  void toPath_LocalDateTime() {
    LocalTime time = LocalTime.of(13, 43);
    LocalDate day = LocalDate.of(2020, 1, 4);
    LocalDateTime dayTime = LocalDateTime.of(day, time);

    assertEquals("2020-01-04T13:43", PathConversion.toPath(dayTime));
    assertEquals("2020-01-04", PathConversion.toPath(day));
  }

}
