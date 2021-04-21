package org.example;

import io.avaje.inject.SystemContext;
import io.helidon.health.HealthSupport;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.Service;
import io.helidon.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

public class Main {

  static {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    startServer(8083);
  }

  static WebServer startServer(int port) {

    WebServer server = WebServer.builder(createRouting())
      //.config(config.get("server"))
      .addMediaSupport(JacksonSupport.create())
      .port(port)
      .build();

    server.start()
      .thenAccept(ws -> {
        log.info("Server is up! http://localhost:" + ws.port() + "/greet");
        ws.whenShutdown().thenRun(() -> log.info("Server is down. Good bye!"));
      })
      .exceptionally(t -> {
        System.err.println("Startup failed: " + t.getMessage());
        t.printStackTrace(System.err);
        return null;
      });

    return server;
  }

  private static Routing createRouting() {

    final Routing.Builder builder = Routing.builder()
      //.register(FormParamsSupport.create())
      //.register( DefaultMediaSupport.formParamReader())
      .register(HealthSupport.create())
      .register(MetricsSupport.create())
      .register("/greet", new GreetService());

    SystemContext.getBeans(Service.class).forEach(builder::register);

    return builder.build();
  }

}
