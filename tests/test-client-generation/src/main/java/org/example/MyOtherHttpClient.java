package org.example;

import io.avaje.http.api.*;

@Client
public interface MyOtherHttpClient {

  @Post
  String asPlainString();

}
