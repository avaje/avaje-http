package io.avaje.http.client;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

class DHttpClientContextBuilder implements HttpClientContext.Builder {

  private HttpClient client;

  private String baseUrl;

  private Duration requestTimeout = Duration.ofSeconds(20);

  private BodyAdapter bodyAdapter;

  private ResponseListener responseListener;

  private CookieHandler cookieHandler = new CookieManager();

  private HttpClient.Redirect redirect = HttpClient.Redirect.NORMAL;

  DHttpClientContextBuilder() {
  }

  @Override
  public HttpClientContext.Builder with(HttpClient client) {
    this.client = client;
    return this;
  }

  @Override
  public HttpClientContext.Builder withBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  @Override
  public HttpClientContext.Builder withRequestTimeout(Duration requestTimeout) {
    this.requestTimeout = requestTimeout;
    return this;
  }

  @Override
  public HttpClientContext.Builder withBodyAdapter(BodyAdapter adapter) {
    this.bodyAdapter = adapter;
    return this;
  }

  @Override
  public HttpClientContext.Builder withResponseListener(ResponseListener responseListener) {
    this.responseListener = responseListener;
    return this;
  }

  @Override
  public HttpClientContext.Builder withCookieHandler(CookieHandler cookieHandler) {
    this.cookieHandler = cookieHandler;
    return this;
  }

  @Override
  public HttpClientContext.Builder withRedirect(HttpClient.Redirect redirect) {
    this.redirect = redirect;
    return this;
  }

  @Override
  public HttpClientContext build() {
    requireNonNull(baseUrl, "baseUrl is not specified");
    requireNonNull(requestTimeout, "requestTimeout is not specified");
    if (client == null) {
      client = defaultClient();
    }
    return new DHttpClientContext(client, baseUrl, requestTimeout, bodyAdapter, responseListener);
  }

  private HttpClient defaultClient() {
    final HttpClient.Builder builder =
      HttpClient.newBuilder()
        .followRedirects(redirect)
        .connectTimeout(Duration.ofSeconds(20));
    if (cookieHandler != null) {
      builder.cookieHandler(cookieHandler);
    }
    return builder.build();
  }

}
