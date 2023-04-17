package io.avaje.http.api.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

class ThreadLocalRequestContextResolverTest {

  RequestContextResolver resolver = new ThreadLocalRequestContextResolver();

  @Test
  void testCallWith() throws Exception {

    resolver.callWith(
        new ServerContext("request", "response"),
        () -> {
          assertThat(resolver.currentRequest().isPresent()).isTrue();
          return 1234;
        });

    assertThat(resolver.currentRequest().isPresent()).isFalse();
  }

  @Test
  void testCallWithRebind() throws Exception {

    assertThatIllegalStateException()
        .isThrownBy(
            () -> {
              resolver.callWith(
                  new ServerContext("request", "response"),
                  () -> {
                    assertThat(resolver.currentRequest().isPresent()).isTrue();
                    return resolver.callWith(
                        resolver.currentRequest().orElseThrow(),
                        () -> {
                          assertThat(resolver.currentRequest().isPresent()).isTrue();
                          return 1234;
                        });
                  });
            });

    assertThat(resolver.currentRequest().isPresent()).isFalse();
  }

  @Test
  void testFuture() throws Exception {

    resolver
        .callWith(
            new ServerContext("request", "response"),
            () -> {
              assertThat(resolver.currentRequest().isPresent()).isTrue();

              return CompletableFuture.supplyAsync(
                  () -> {
                    assertThat(resolver.currentRequest().isPresent()).isFalse();

                    return "d";
                  });
            })
        .join();

    assertThat(resolver.currentRequest().isPresent()).isFalse();
  }

  @Test
  void testSupplyWith() {

    resolver.supplyWith(
        new ServerContext("request", "response"),
        () -> {
          assertThat(resolver.currentRequest().isPresent()).isTrue();
          return 1234;
        });

    assertThat(resolver.currentRequest().isPresent()).isFalse();
  }

  @Test
  void testRunWith() throws Exception {

    resolver.runWith(
        new ServerContext("request", "response"),
        () -> {
          assertThat(resolver.currentRequest().isPresent()).isTrue();
        });

    assertThat(resolver.currentRequest().isPresent()).isFalse();
  }
}
