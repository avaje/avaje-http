package io.avaje.http.generator;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Secondary;
import io.avaje.jsonb.Jsonb;

@Factory
public class JsonBFactory {
  @Bean
  @Secondary
  Jsonb jsonB() {
    return Jsonb.builder().failOnUnknown(false).build();
  }
}
