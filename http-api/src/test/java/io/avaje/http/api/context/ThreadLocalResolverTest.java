package io.avaje.http.api.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ThreadLocalResolverTest {

  HttpRequestContextResolver resolver = new ThreadLocalResolver();

  @Test
  void testCallWith() throws Exception {

    resolver.callWith(
        () -> "context",
        () -> {
          assertThat(resolver.<String>currentRequest().isPresent()).isTrue();
          return 1234;
        });

    assertThat(resolver.<String>currentRequest().isPresent()).isFalse();
  }

  @Test
  void testSupplyWith() {

    resolver.supplyWith(
        () -> "context",
        () -> {
          assertThat(resolver.currentRequest().isPresent()).isTrue();
          return 1234;
        });

    assertThat(resolver.currentRequest().isPresent()).isFalse();
  }

  @Test
  void testRunWith() throws Exception {

    resolver.runWith(
        () -> "context",
        () -> {
          assertThat(resolver.currentRequest().isPresent()).isTrue();
        });

    assertThat(resolver.currentRequest().isPresent()).isFalse();
  }
}
