package io.avaje.http.generator.client.clients;

import io.avaje.http.api.BodyString;
import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.Post;

@Client(generate = false)
public interface UserClient {

  @Post("/users")
  String createUser(@BodyString String body);

  @Post("/body")
  String bodies(Body... bodies);

  @Get("/users/{userId}")
  String getUserById(String userId);
}
