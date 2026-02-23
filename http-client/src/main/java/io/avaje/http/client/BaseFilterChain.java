package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.Iterator;

import io.avaje.http.client.RequestIntercept.InterceptChain;

final class BaseFilterChain implements InterceptChain {

  private final Iterator<RequestIntercept> filters;
  HttpResponse<?> response;

  BaseFilterChain(Iterator<RequestIntercept> filters, HttpClientRequest request) {
    this.filters = filters;
  }

  @Override
  public HttpResponse<?> proceed(HttpClientRequest request) {
    if (filters.hasNext()) {
      filters.next().intercept(request, this);
      return response;
    }
    if (response == null) {
      throw new IllegalStateException(
          "No response set. An interceptor that doesn't call proceed(), must set the response.");
    }
    return response;
  }

  @Override
  public void setResponse(HttpResponse<?> response) {
    this.response = response;
  }
}
