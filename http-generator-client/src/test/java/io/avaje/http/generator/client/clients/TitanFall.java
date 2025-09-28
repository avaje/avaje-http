package io.avaje.http.generator.client.clients;

import java.util.List;
import java.util.Map;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.Headers;
import io.avaje.http.api.SuppressLogging;

@Client
@SuppressLogging
@Headers("Content-Type: applicaton/json")
public interface TitanFall {

  @Get("/${titan}/${drop.point}")
  @Headers("Something: \\invalid\n\t")
  Titan titanFall();

  @Get("/masterpiece")
  Map<String, List<Titan>> titanFall2();


  @Get("/${titan}/copium")
  @Headers("      Accept    :   applicaton/json")
  Titan titanFall3();
}