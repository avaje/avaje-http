package org.example.web;

import io.avaje.http.api.WebRoutes;
import io.avaje.inject.SystemContext;
import spark.Spark;

public class App {

  public static void main(String[] args) {

    JsonTransformer jsonTransformer = new JsonTransformer();

    Spark.port(8082);
    Spark.defaultResponseTransformer(jsonTransformer);
    Spark.get("/", (request, response) -> {
      response.status(200);
      return "hello";
    });

    SystemContext.getBeans(WebRoutes.class)
      .forEach(WebRoutes::registerRoutes);
  }
}
