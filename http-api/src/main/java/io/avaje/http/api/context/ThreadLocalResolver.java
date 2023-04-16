package io.avaje.http.api.context;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class ThreadLocalResolver implements HttpRequestContextResolver {

  private static final ThreadLocal<RequestContext<?>> REQUEST = new ThreadLocal<>();

  @Override
  public <T> T callWith(RequestContext<?> request, Callable<T> callable) throws Exception {
    try {
      set(request);
      return callable.call();
    } finally {
      REQUEST.remove();
    }
  }

  @Override
  public <T> T supplyWith(RequestContext<?> request, Supplier<T> supplier) {
    try {
      set(request);
      return supplier.get();
    } finally {
      REQUEST.remove();
    }
  }

  @Override
  public void runWith(RequestContext<?> request, Runnable runnable) {
    try {
      set(request);
      runnable.run();
    } finally {
      REQUEST.remove();
    }
  }

  private void set(RequestContext<?> request) {
    if (request == null) {
      REQUEST.remove();
    } else {
      REQUEST.set(request);
    }
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public <T> Optional<T> currentRequest() {
    return (Optional<T>) Optional.ofNullable(REQUEST.get()).map(RequestContext::getRequest);
  }
}
