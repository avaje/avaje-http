package org.example.pkgprivate;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.Header;

@Client
interface PrivateClient {

  @Get("/private")
  String apiCall(@Header("Accept") String accept);

}
