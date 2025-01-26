package io.avaje.http.generator.client.clients;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.Header;

@Client
public interface PrivateClient2 {

  @Get("/private")
  String apiCall(@Header("Accept") String accept);

}
