package org.example;

import io.dinject.SystemContext;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.Service;
import io.helidon.webserver.WebServer;

import java.io.IOException;
import java.util.List;

public class Main {

  public static void main(String[] args) throws IOException {
    startServer();
  }

  static WebServer startServer() throws IOException {

    //setupLogging();

    // By default this will pick up application.yaml from the classpath
    //Config config = Config.create();

    JacksonSupport jacksonSupport = JacksonSupport.create();


    // Build server with JSONP support
    WebServer server = WebServer.builder(createRouting())
      //.config(config.get("server"))
//      .addMediaSupport(JsonpSupport.create())
      .addMediaSupport(jacksonSupport)
      .port(8083)
      .build();

    // Try to start the server. If successful, print some info and arrange to
    // print a message at shutdown. If unsuccessful, print the exception.
    server.start()
      .thenAccept(ws -> {
        System.out.println(
          "WEB server is up! http://localhost:" + ws.port() + "/greet");
        ws.whenShutdown().thenRun(()
          -> System.out.println("WEB server is DOWN. Good bye!"));
      })
      .exceptionally(t -> {
        System.err.println("Startup failed: " + t.getMessage());
        t.printStackTrace(System.err);
        return null;
      });

    // Server threads are not daemon. No need to block. Just react.

    return server;
  }

  private static Routing createRouting() {

    MetricsSupport metrics = MetricsSupport.create();
    HealthSupport health = HealthSupport.builder()
      .addLiveness(HealthChecks.healthChecks())   // Adds a convenient set of checks
      .build();


    final Routing.Builder builder = Routing.builder()
      .register(health)                   // Health at "/health"
      .register(metrics)                  // Metrics at "/metrics"
      .register("/greet", new GreetService());

    final List<Service> services = SystemContext.getBeans(Service.class);
    services.forEach(builder::register);

    return builder.build();
  }

//  /**
//   * Configure logging from logging.properties file.
//   */
//  private static void setupLogging() throws IOException {
//    try (InputStream is = Main.class.getResourceAsStream("/logging.properties")) {
//      LogManager.getLogManager().readConfiguration(is);
//    }
//  }
}
