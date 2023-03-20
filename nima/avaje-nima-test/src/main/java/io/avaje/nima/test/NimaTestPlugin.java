package io.avaje.nima.test;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpClientContext;
import io.avaje.inject.BeanScope;
import io.avaje.inject.test.Plugin;
import io.avaje.nima.Nima;

import java.lang.annotation.Annotation;

/**
 * avaje-inject-test plugin that:
 * <p>
 * - Detects when a http client is being used in a test
 * - Starts Nima server on random port
 * - Creates the appropriate client for the port (to be injected into the test class)
 * - Shutdown the server on test completion (Plugin.Scope close)
 */
public final class NimaTestPlugin implements Plugin {

  private static final String AVAJE_HTTP_CLIENT = "io.avaje.http.api.Client";
  private static final String AVAJE_HTTP_PATH = "io.avaje.http.api.Path";

  /**
   * Return true if it's a http client this plugin supports.
   */
  @Override
  public boolean forType(Class<?> type) {
    return HttpClient.class.equals(type) || isHttpClientApi(type);
  }

  private boolean isHttpClientApi(Class<?> type) {
    if (!type.isInterface()) {
      return false;
    }
    for (Annotation annotation : type.getAnnotations()) {
      String name = annotation.annotationType().getName();
      if (AVAJE_HTTP_CLIENT.equals(name)) {
        return true;
      }
      if (AVAJE_HTTP_PATH.equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Create a scope.
   * <p>
   * The scope will contain a server + client pair.
   */
  @Override
  public Scope createScope(BeanScope beanScope) {
    return new LocalScope(beanScope);
  }

  private static class LocalScope implements Plugin.Scope {

    private final Nima server;
    private final HttpClient httpClient;

    LocalScope(BeanScope beanScope) {
      this.server = beanScope.getOptional(Nima.class)
        .orElse(new Nima())
        .configure(beanScope)
        .port(0);

      // get a HttpClientContext.Builder provided by dependency injection test scope or new one up
      server.start();
      int port = server.port();
      this.httpClient = beanScope.getOptional(HttpClient.Builder.class)
        .orElse(HttpClient.builder())
        .configureWith(beanScope)
        .baseUrl("http://localhost:" + port)
        .build();
    }

    @Override
    public Object create(Class<?> type) {
      if (HttpClient.class.equals(type)) {
        return httpClient;
      }
      return apiClient(type);
    }

    private Object apiClient(Class<?> clientInterface) {
      return httpClient.create(clientInterface);
    }

    @Override
    public void close() {
      server.stop();
    }
  }
}
