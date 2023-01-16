package io.avaje.http.client;

import java.time.Duration;

/**
 * The HTTP client context that we use to build and process requests.
 *
 * <pre>{@code
 *
 *   HttpClient client = HttpClient.builder()
 *       .baseUrl("http://localhost:8080")
 *       .bodyAdapter(new JacksonBodyAdapter())
 *       .build();
 *
 *  HelloDto dto = client.request()
 *       .path("hello")
 *       .queryParam("name", "Rob")
 *       .queryParam("say", "Whats up")
 *       .GET()
 *       .bean(HelloDto.class);
 *
 * }</pre>
 */
public interface HttpClient {

  interface Builder {

    /**
     * The state of the builder with methods to read the set state.
     */
    interface State {

      /**
       * Return the base URL.
       */
      String baseUrl();

      /**
       * Return the body adapter.
       */
      BodyAdapter bodyAdapter();

      /**
       * Return the HttpClient.
       */
      java.net.http.HttpClient client();

      /**
       * Return true if requestLogging is on.
       */
      boolean requestLogging();

      /**
       * Return the request timeout.
       */
      Duration requestTimeout();

      /**
       * Return the retry handler.
       */
      RetryHandler retryHandler();
    }
  }

  /**
   * Statistic metrics collected to provide an overview of activity of this client.
   */
  interface Metrics {

    /**
     * Return the total number of responses.
     */
    long totalCount();

    /**
     * Return the total number of error responses (status code >= 300).
     */
    long errorCount();

    /**
     * Return the total response bytes (excludes streaming responses).
     */
    long responseBytes();

    /**
     * Return the total response time in microseconds.
     */
    long totalMicros();

    /**
     * Return the max response time in microseconds (since the last reset).
     */
    long maxMicros();

    /**
     * Return the average response time in microseconds.
     */
    long avgMicros();
  }
}
