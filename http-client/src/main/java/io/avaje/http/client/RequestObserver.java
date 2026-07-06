package io.avaje.http.client;

import java.net.http.HttpResponse;

/**
 * Observe outgoing requests.
 *
 * <p>A {@link RequestObserver} starts once per logical request execution and can observe each
 * actual send attempt, including retry attempts.
 */
public interface RequestObserver {

  /**
   * No-op request observer.
   */
  RequestObserver NOOP = request -> Observation.NOOP;

  /**
   * Start observing a logical request execution.
   *
   * @param request The request about to be executed
   * @return The observation to use for actual send attempts
   */
  Observation start(HttpClientRequest request);

  /**
   * Observation of a logical request execution.
   */
  interface Observation {

    /**
     * No-op request observation.
     */
    Observation NOOP = (request, resendCount) -> Attempt.NOOP;

    /**
     * Start observing an actual send attempt.
     *
     * @param request The request about to be sent
     * @param resendCount The send attempt count where 0 is the first send and 1+ are retries
     * @return The attempt observer
     */
    Attempt startAttempt(HttpClientRequest request, int resendCount);
  }

  /**
   * Observation of a single send attempt.
   */
  interface Attempt {

    /**
     * No-op request attempt observer.
     */
    Attempt NOOP = new Attempt() {
      @Override
      public void onResponse(HttpResponse<?> response) {
      }

      @Override
      public void onError(Throwable error) {
      }
    };

    /**
     * Invoked when the send attempt returns a response.
     *
     * @param response The response
     */
    void onResponse(HttpResponse<?> response);

    /**
     * Invoked when the send attempt fails before a response is received.
     *
     * @param error The error
     */
    void onError(Throwable error);
  }
}
