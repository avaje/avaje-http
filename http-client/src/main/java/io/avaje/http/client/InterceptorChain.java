package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Processing of multiple RequestIntercept.
 *
 * <p>Noting that afterResponse interceptors are processed in reverse order.
 */
final class InterceptorChain implements RequestIntercept.InterceptChain {

  private final Iterator<RequestIntercept> intercepts;
  private final Supplier<HttpResponse<?>> callInvocation;
  private HttpResponse<?> response;

  InterceptorChain(List<RequestIntercept> interceptors, Supplier<HttpResponse<?>> request) {
    this.intercepts = List.copyOf(interceptors).iterator();
    this.callInvocation = request;
  }

  @Override
  public HttpResponse<?> proceed(HttpClientRequest request) {
    if (intercepts.hasNext()) {
      intercepts.next().intercept(request, this);
    }
    if (response != null) {
      return response;
    }
    setResponse(this.callInvocation.get());
    return response;
  }

  @Override
  public void setResponse(HttpResponse<?> response) {
    this.response = response;
  }
}
