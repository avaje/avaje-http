package io.avaje.http.client;

import io.avaje.applog.AppLog;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static java.lang.System.Logger.Level.*;

/**
 * Service loads the HttpApiProvider for HttpApi.
 */
final class DHttpApi {

  private static final System.Logger log = AppLog.getLogger("io.avaje.http.client");

  private static final DHttpApi INSTANCE = new DHttpApi();

  private final Map<Class<?>, HttpApiProvider<?>> providerMap = new HashMap<>();

  DHttpApi() {
    init();
  }

  @SuppressWarnings("rawtypes")
  void init() {
    for (HttpApiProvider apiProvider : ServiceLoader.load(HttpApiProvider.class)) {
      addProvider(apiProvider);
    }
    log.log(DEBUG, "providers for {0}", providerMap.keySet());
  }

  void addProvider(HttpApiProvider apiProvider) {
    providerMap.put(apiProvider.type(), apiProvider);
  }

  @SuppressWarnings("unchecked")
  private <T> HttpApiProvider<T> lookup(Class<T> type) {
    return (HttpApiProvider<T>) providerMap.get(type);
  }

  @SuppressWarnings("unchecked")
  <T> T provideFor(Class<T> type, HttpClientContext clientContext) {
    final HttpApiProvider<T> apiProvider = lookup(type);
    if (apiProvider == null) {
      throw new IllegalArgumentException("No registered HttpApiProvider for type: " + type);
    }
    return apiProvider.provide(clientContext);
  }

  /**
   * Return the client implementation via service loading.
   */
  static <T> T provide(Class<T> type, HttpClientContext clientContext) {
    return INSTANCE.provideFor(type, clientContext);
  }

  /**
   * Return the HttpApiProvider for the client interface type or null if not registered.
   */
  static <T> HttpApiProvider<T> get(Class<T> type) {
    return INSTANCE.lookup(type);
  }
}
