package io.avaje.http.generator.client.clients;

import io.avaje.http.api.Client;
import io.avaje.http.api.Patch;

@Client
public interface ExampleClient extends UserClient {
  @Patch
  void patchy();
}
