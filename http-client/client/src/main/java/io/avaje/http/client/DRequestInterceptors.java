package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Processing of multiple RequestIntercept.
 * <p>
 * Noting that afterResponse interceptors are processed in reverse order.
 */
class DRequestInterceptors implements RequestIntercept {

  private final List<RequestIntercept> before;
  private final List<RequestIntercept> after;

  DRequestInterceptors(List<RequestIntercept> interceptors) {
    this.before = new ArrayList<>(interceptors);
    Collections.reverse(interceptors);
    this.after = new ArrayList<>(interceptors);
  }

  @Override
  public void beforeRequest(HttpClientRequest request) {
    for (RequestIntercept interceptor : before) {
      interceptor.beforeRequest(request);
    }
  }

  @Override
  public void afterResponse(HttpResponse<?> response, HttpClientRequest request) {
    for (RequestIntercept interceptor : after) {
      interceptor.afterResponse(response, request);
    }
  }
}
