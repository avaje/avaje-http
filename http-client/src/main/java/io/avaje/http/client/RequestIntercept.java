package io.avaje.http.client;

import java.net.http.HttpResponse;

import com.sun.net.httpserver.HttpExchange;

/** Interceptor for before the request is made and after the response is obtained. */
public interface RequestIntercept {

  /**
   * Before the request has been made.
   *
   * <p>Typically we can add headers or modify the request prior to it being sent.
   */
  default void beforeRequest(HttpClientRequest request) {
    // do nothing by default
  }

  /**
   * Intercept the request
   *
   * <p>This is a more powerful method that allows you to modify the request and return a new one.
   */
  default void intercept(HttpClientRequest request, InterceptChain chain) {
    beforeRequest(request);
    afterResponse(chain.proceed(request), request);
  }

  /** After the response has been received. */
  default void afterResponse(HttpResponse<?> response, HttpClientRequest request) {
    // do nothing by default
  }

  /**
   * Filter chain that contains all subsequent filters that are configured, as well as the final
   * route.
   */
  interface InterceptChain {

    /**
     * Calls the next interceptor in the chain, or else the user's exchange handler, if this is the
     * final filter in the chain. The {@link RequestIntercept} may decide to terminate the chain, by
     * not calling this method. In this case, the filter <b>must</b> send the response to the
     * request, because the application's {@linkplain HttpExchange exchange} handler will not be
     * invoked.
     */
    HttpResponse<?> proceed(HttpClientRequest request);

    /**
     * Set the response to be returned to the caller. This is used when the interceptor decides to
     * terminate the chain.
     */
    void setResponse(HttpResponse<?> response);
  }
}
