package io.avaje.http.generator.client.clients;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.Header;

@Client
public interface ApiClient {

  @Get("/inputstream")
  String apiCall(@Header("Accept") String accept);

  @Get("/mapped")
  String mapped(@Header("Accept") String accept) throws MappedException;
}
