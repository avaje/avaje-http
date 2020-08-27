package io.avaje.http.client;

public interface HttpApiProvider<T> {

  T provide(HttpClientContext client);

}
