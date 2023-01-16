package org.example.dinject;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.jsonb.Jsonb;

@Factory
class MyDIConfig {

  @Bean
  Jsonb jsonb() {
    return  Jsonb.builder().build();
  }

}
