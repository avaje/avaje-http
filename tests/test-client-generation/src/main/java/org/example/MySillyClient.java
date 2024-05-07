package org.example;

import io.avaje.http.api.*;

@Client
public interface MySillyClient {

  @Post
  String asPlainString();

}
