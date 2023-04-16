package io.avaje.http.api.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

class ThreadLocalRequestContextResolverTest {

  RequestContextResolver resolver = new ThreadLocalRequestContextResolver();

  @Test
  void testCallWith() throws Exception {

    resolver.callWith(
        "context",
        () -> {
          assertThat(resolver.<String>currentRequest().isPresent()).isTrue();
          return 1234;
        });

    assertThat(resolver.<String>currentRequest().isPresent()).isFalse();
  }

  @Test
  void testFuture() throws Exception {

    resolver.callWith(
        "context",
        () -> {
          assertThat(resolver.<String>currentRequest().isPresent()).isTrue();

          return CompletableFuture.supplyAsync(
              () -> {
                assertThat(resolver.<String>currentRequest().isPresent()).isFalse();

                return "d";
              });
        }).join();

    assertThat(resolver.<String>currentRequest().isPresent()).isFalse();
  }

  @Test
  void testSupplyWith() {

    resolver.supplyWith(
        "context",
        () -> {
          assertThat(resolver.currentRequest().isPresent()).isTrue();
          return 1234;
        });

    assertThat(resolver.currentRequest().isPresent()).isFalse();
  }

  @Test
  void testRunWith() throws Exception {

    resolver.runWith(
        "context",
        () -> {
          assertThat(resolver.currentRequest().isPresent()).isTrue();
        });

    assertThat(resolver.currentRequest().isPresent()).isFalse();
  }
}
