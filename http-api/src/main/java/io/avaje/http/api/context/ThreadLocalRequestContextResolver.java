package io.avaje.http.api.context;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class ThreadLocalRequestContextResolver implements RequestContextResolver {

  private static final ThreadLocal<ServerContext> REQUEST = new ThreadLocal<>();

  @Override
  public <T> T callWith(ServerContext request, Callable<T> callable) throws Exception {
    throwIfSet();
    try {
      REQUEST.set(request);
      return callable.call();
    } finally {
      REQUEST.remove();
    }
  }

  @Override
  public <T> T supplyWith(ServerContext request, Supplier<T> supplier) {
    throwIfSet();
    try {
      REQUEST.set(request);
      return supplier.get();
    } finally {
      REQUEST.remove();
    }
  }

  @Override
  public void runWith(ServerContext request, Runnable runnable) {
    throwIfSet();
    try {
      REQUEST.set(request);
      runnable.run();
    } finally {
      REQUEST.remove();
    }
  }

  @Override
  public Optional<ServerContext> currentRequest() {
    return Optional.ofNullable(REQUEST.get());
  }

  private void throwIfSet() {
    if (REQUEST.get() != null) {
      throw new IllegalStateException("Rebinding the ServerContext is not permitted");
    }
  }
}
