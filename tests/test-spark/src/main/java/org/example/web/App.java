package org.example.web;

import io.avaje.http.api.WebRoutes;
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

    ApplicationScope.list(WebRoutes.class)
      .forEach(WebRoutes::registerRoutes);
  }
}
