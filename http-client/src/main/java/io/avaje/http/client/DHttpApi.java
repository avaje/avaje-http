package io.avaje.http.client;

import io.avaje.applog.AppLog;
import io.avaje.http.client.HttpClient.GeneratedComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static java.lang.System.Logger.Level.*;

/** Service loads the HttpApiProvider for HttpApi. */
final class DHttpApi {

  private static final System.Logger log = AppLog.getLogger("io.avaje.http.client");

  private static final DHttpApi INSTANCE = new DHttpApi();

  private final Map<Class<?>, HttpApiProvider<?>> providerMap = new HashMap<>();

  DHttpApi() {
    this(Thread.currentThread().getContextClassLoader());
  }

  DHttpApi(ClassLoader classLoader) {
    for (final var apiProvider : ServiceLoader.load(GeneratedComponent.class, classLoader)) {
      apiProvider.register(providerMap);
    }
    log.log(DEBUG, "providers for {0}", providerMap.keySet());
  }

  <T> void addProvider(Class<T> type, HttpApiProvider<?> apiProvider) {
    providerMap.put(type, apiProvider);
  }

  @SuppressWarnings("unchecked")
  <T> T provideFor(Class<T> type, HttpClient httpClient) {
    final var apiProvider = (HttpApiProvider<T>) providerMap.get(type);
    if (apiProvider == null) {
      throw new IllegalArgumentException("No registered HttpApiProvider for type: " + type);
    }
    return apiProvider.provide(httpClient);
  }

  /** Return the client implementation via service loading. */
  static <T> T get(Class<T> type, HttpClient httpClient) {
    return INSTANCE.provideFor(type, httpClient);
  }

  static <T> T get(Class<T> clientInterface, HttpClient httpClient, ClassLoader classLoader) {
    return new DHttpApi(classLoader).provideFor(clientInterface, httpClient);
  }

}
