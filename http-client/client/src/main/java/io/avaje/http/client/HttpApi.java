package io.avaje.http.client;

/**
 * Provides Http client service implementations for a given interface type.
 */
public interface HttpApi {

  /**
   * Provide the http client implementation for the given interface type.
   *
   * @param interfaceType The interface type
   * @param clientContext The http client context used for executing the requests
   * @return The http client implementation for the given interface type
   */
  static <T> T provide(Class<T> interfaceType, HttpClientContext clientContext) {
    return DHttpApi.provide(interfaceType, clientContext);
  }
}
