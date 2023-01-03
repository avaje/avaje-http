package org.example.myapp;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.jsonb.Jsonb;

@Factory
public class JsonBFactory {
  @Bean
  Jsonb jsonB() {
    return Jsonb.builder().failOnUnknown(false).build();
  }
}
