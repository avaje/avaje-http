package org.example.github;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpException;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.http.client.RetryHandler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GithubTest {

  @Test
  @Disabled
  void test() {

    final HttpClient clientContext =
        HttpClient.builder()
            .baseUrl("https://api.github.com")
            .bodyAdapter(new JacksonBodyAdapter())
            .requestLogging(false)
            .build();

    // will not work under module classpath without registering the HttpApiProvider
    final Simple simple = clientContext.create(Simple.class);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();

    clientContext
        .request()
        .path("users")
        .path("rbygrave")
        .path("repos")
        .GET()
        .async()
        .asString()
        .thenAccept(
            res -> {
              System.out.println("RES: " + res.statusCode());
              System.out.println("BODY: " + res.body().substring(0, 150) + "...");
            })
        .join();
  }

  @Test
  void testExceptionMapping() {

    final HttpClient clientContext =
        HttpClient.builder()
            .baseUrl("https://bogus-link")
            .globalErrorMapper(e -> new IllegalStateException())
            .bodyAdapter(new JacksonBodyAdapter())
            .requestLogging(false)
            .build();

    // will not work under module classpath without registering the HttpApiProvider
    final Simple simple = clientContext.create(Simple.class);

    assertThatThrownBy(() -> simple.listRepos("rbygrave", "junk"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testExceptionMappingAsync() {

    final HttpClient clientContext =
        HttpClient.builder()
            .baseUrl("https://bogus-link")
            .bodyAdapter(new JacksonBodyAdapter())
            .globalErrorMapper(e -> new IllegalStateException())
            .requestLogging(false)
            .build();

    assertThatThrownBy(
            () ->
                clientContext
                    .request()
                    .path("junk")
                    .errorMapper(e -> new IllegalArgumentException())
                    .GET()
                    .async()
                    .asString()
                    .join())
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class);

    // global error mapper used
    assertThatThrownBy(
      () ->
        clientContext
          .request()
          .path("junk")
          .GET()
          .async()
          .asString()
          .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(IllegalStateException.class);
  }

  @Test
  void testSyncRetry() {

    final HttpClient clientContext =
        HttpClient.builder()
            .baseUrl("https://bogus-link")
            .globalErrorMapper(e -> new IllegalStateException())
            .bodyAdapter(new JacksonBodyAdapter())
            .retryHandler(new Retry())
            .requestLogging(false)
            .build();

    // will not work under module classpath without registering the HttpApiProvider
    final Simple simple = clientContext.create(Simple.class);

    assertThatThrownBy(() -> simple.listRepos("rbygrave", "junk"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testAsyncRetry() {

    final HttpClient clientContext =
        HttpClient.builder()
            .baseUrl("https://bogus-link")
            .bodyAdapter(new JacksonBodyAdapter())
            .globalErrorMapper(e -> new IllegalStateException())
            .retryHandler(new Retry())
            .requestLogging(false)
            .build();

    assertThatThrownBy(
            () ->
                clientContext
                    .request()
                    .path("users")
                    .path("rbygrave")
                    .path("repos")
                    .errorMapper(e -> new IllegalArgumentException())
                    .GET()
                    .async()
                    .asString()
                    .join())
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class);
  }

  static class Retry implements RetryHandler {

    @Override
    public boolean isRetry(int retryCount, HttpResponse<?> response) {

      return false;
    }

    @Override
    public boolean isExceptionRetry(int retryCount, HttpException exception) {

      return retryCount < 3;
    }
  }
}
