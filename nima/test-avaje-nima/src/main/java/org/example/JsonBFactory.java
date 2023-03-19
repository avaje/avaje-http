package org.example;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Secondary;
import io.avaje.jsonb.Jsonb;
import jakarta.inject.Provider;

//@Factory
public class JsonBFactory {
//  @Secondary
//  @Bean
  Provider<Jsonb> jsonB() {
    return () -> {
      return Jsonb.builder().failOnUnknown(false).build();
    };
  }
}
