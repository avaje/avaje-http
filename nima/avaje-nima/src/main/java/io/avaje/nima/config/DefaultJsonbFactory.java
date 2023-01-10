package io.avaje.nima.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.InjectModule;
import io.avaje.inject.Secondary;
import io.avaje.jsonb.Jsonb;

@InjectModule(name = "nima-defaults", provides = Jsonb.class)
@Factory
public class DefaultJsonbFactory {

  @Secondary
  @Bean
  Jsonb jsonb() {
    return Jsonb.builder().build();
  }
}
