package io.avaje.http.client;

import static java.lang.System.Logger.Level.DEBUG;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import io.avaje.applog.AppLog;
import io.avaje.http.client.HttpClient.GeneratedComponent;

/** Service loads the HttpApiProvider for HttpApi. */
final class DHttpApi {

  private static final System.Logger log = AppLog.getLogger("io.avaje.http.client");
  private static final DHttpApi INSTANCE = new DHttpApi();
  private final Map<Class<?>, HttpApiProvider<?>> providerMap = new HashMap<>();

  DHttpApi() {
    init(Thread.currentThread().getContextClassLoader());
  }

  void init(ClassLoader loader) {
    for (final var apiProvider : ServiceLoader.load(GeneratedComponent.class, loader)) {
      apiProvider.register(providerMap);
    }
    log.log(DEBUG, "providers for {0}", providerMap.keySet());
  }

  <T> void addProvider(Class<T> type, HttpApiProvider<?> apiProvider) {
    providerMap.put(type, apiProvider);
  }

  @SuppressWarnings("unchecked")
  <T> T provideFor(Class<T> type, HttpClient httpClient, ClassLoader classLoader) {
    var apiProvider = (HttpApiProvider<T>) providerMap.get(type);

    if (apiProvider == null) {
      init(classLoader);
      apiProvider = (HttpApiProvider<T>) providerMap.get(type);
    }

    if (apiProvider == null) {
      throw new IllegalArgumentException("No registered HttpApiProvider for type: " + type);
    }
    return apiProvider.provide(httpClient);
  }

  /** Return the client implementation via service loading. */
  static <T> T get(Class<T> type, HttpClient httpClient, ClassLoader classLoader) {
    return INSTANCE.provideFor(type, httpClient, classLoader);
  }
}
