package io.avaje.http.api.context;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * The holder for the current request context that is bound to instrumented threads. Allowing lookup
 * of the current request if it is present. The Default implementation uses ThreadLocal. If you are
 * able, you should provide an implementation using ScopedValues.
 */
public interface RequestContextResolver {

  /**
   * Wraps the execution of the given callable in request context processing.
   *
   * @param ctx The request context
   * @param callable The callable
   * @param <T> The return type of the callable
   * @return The return value of the callable
   * @throws Exception if the callable throws an exception
   */
  <T> T callWith(ServerContext ctx, Callable<T> callable) throws Exception;

  /**
   * Wraps the execution of the given supplier in request context processing.
   *
   * @param ctx The request context
   * @param supplier The supplier
   * @param <T> The return type of the supplier
   * @return The return value of the supplier
   */
  <T> T supplyWith(ServerContext ctx, Supplier<T> supplier);

  /**
   * Wraps the execution of the given runnable in request context processing.
   *
   * @param ctx The request context
   * @param runnable The runnable
   */
  void runWith(ServerContext request, Runnable runnable);

  /**
   * Retrieve the current server context.
   *
   * @return The request context if it is present
   */
  Optional<ServerContext> currentRequest();
}
