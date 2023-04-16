package io.avaje.http.api.context;
/**
 * A container for the actual Http request object.
 *
 * @param <T> the type of the context object.
 */
@FunctionalInterface
public interface RequestContext<T> {

  T getRequest();
}
