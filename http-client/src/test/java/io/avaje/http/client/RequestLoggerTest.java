package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestLoggerTest {

  private final RequestLogger requestLogger = new RequestLogger();

  @Test
  void obfuscate() {
    assertTrue(requestLogger.obfuscate("Authorization"));
    assertFalse(requestLogger.obfuscate("Foo"));
  }
}
