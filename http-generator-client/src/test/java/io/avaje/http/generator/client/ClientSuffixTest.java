package io.avaje.http.generator.client;

import org.junit.jupiter.api.Test;

import static io.avaje.http.generator.client.ClientSuffix.*;
import static org.junit.jupiter.api.Assertions.*;

class ClientSuffixTest {

  @Test
  void testFromInterface() {
    assertEquals("Impl", fromInterface("foo.MyClient"));
    assertEquals("Impl", fromInterface("foo.MyHttpClient"));
    assertEquals("HttpClient", fromInterface("foo.My"));
  }

  @Test
  void testRemoveSuffix() {
    assertEquals("foo.MyClient", removeSuffix("foo.MyClientImpl"));
    assertEquals("foo.MyHttpClient", removeSuffix("foo.MyHttpClientImpl"));
    assertEquals("foo.My", removeSuffix("foo.MyHttpClient"));
  }

  @Test
  void testToInterface() {
    assertEquals("foo.MyClient", toInterface("foo.httpclient.MyClientImpl"));
    assertEquals("foo.MyHttpClient", toInterface("foo.httpclient.MyHttpClientImpl"));
    assertEquals("foo.My", toInterface("foo.httpclient.MyHttpClient"));
  }
}
