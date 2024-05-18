package io.avaje.http.generator.client.clients;

import io.avaje.http.api.BeanParam;
import io.avaje.http.api.Client;
import io.avaje.http.api.Patch;
import io.avaje.http.api.Post;

@Client
public interface ExampleClient2 extends ExampleClient {
  @Patch
  void patchy2();

  @Post
  void beanParam(@BeanParam Params s);
}
