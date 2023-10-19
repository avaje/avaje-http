package io.avaje.http.generator.client.clients;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;

@Client
public interface TitanFall {

  @Get("/${titan}/${drop.point}")
  Titan titanfall();
}