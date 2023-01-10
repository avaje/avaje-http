package org.example.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.helidon.common.context.Context;
import io.helidon.nima.webserver.http.HttpRouting;

@Factory
public class WebServerConfig {

  //@Exclude(Context.Builder.class)
  @Bean
  HttpRouting.Builder routing() {
    var builder = HttpRouting.builder();
    builder.get("/health", (req, res) -> {
      res.send("ok");
    });

    return builder;
  }
}
