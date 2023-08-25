package example.github.httpclient;

import java.util.Map;

import example.github.Simple;
import io.avaje.http.client.HttpApiProvider;
import io.avaje.http.client.HttpClient;

public class GeneratedHttpComponent implements HttpClient.GeneratedComponent {

  @Override
  public void register(Map<Class<?>, HttpApiProvider<?>> providerMap) {
    providerMap.put(Simple.class, Simple$HttpClient::new);
  }
}
