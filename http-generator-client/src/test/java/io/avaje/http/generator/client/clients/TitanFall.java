package io.avaje.http.generator.client.clients;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.Headers;

@Client
@Headers("Content-Type: applicaton/json")
public interface TitanFall {

  @Get("/${titan}/${drop.point}")
  @Headers("Something: \\invalid\n\t")
  Titan titanFall();


  @Get("/${titan}/copium")
  @Headers("      Accept    :   applicaton/json")
  Titan titanFall3();
}