package io.avaje.http.client;

import java.net.http.HttpResponse;

/**
 * Interceptor for before the request is made and after the response is obtained.
 */
public interface RequestIntercept {

  /**
   * Before the request has been made.
   * <p>
   * Typically we can add headers or modify the request prior to it being sent.
   */
  default void beforeRequest(HttpClientRequest request) {
    // do nothing by default
  }

  /**
   * After the response has been received.
   */
  default void afterResponse(HttpResponse<?> response, HttpClientRequest request) {
    // do nothing by default
  }

}
