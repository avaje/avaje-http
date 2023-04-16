package io.avaje.http.api.context;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class ThreadLocalRequestContextResolver implements RequestContextResolver {

  private static final ThreadLocal<Object> REQUEST = new ThreadLocal<>();

  @Override
  public <T> T callWith(Object request, Callable<T> callable) throws Exception {
    try {
      set(request);
      return callable.call();
    } finally {
      REQUEST.remove();
    }
  }

  @Override
  public <T> T supplyWith(Object request, Supplier<T> supplier) {
    try {
      set(request);
      return supplier.get();
    } finally {
      REQUEST.remove();
    }
  }

  @Override
  public void runWith(Object request, Runnable runnable) {
    try {
      set(request);
      runnable.run();
    } finally {
      REQUEST.remove();
    }
  }

  private void set(Object request) {
    if (request == null) {
      REQUEST.remove();
    } else {
      REQUEST.set(request);
    }
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public <T> Optional<T> currentRequest() {
    return (Optional<T>) Optional.ofNullable(REQUEST.get());
  }
}
