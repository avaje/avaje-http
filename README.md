# avaje-http

Http server and client libraries and code generation.

## http server

A jax-rs style controllers with annotations (`@Path`, `@Get` ...)
that is lightweight by using source code generation (annotation processors)
to generate adapter code for Javalin and Helidon SE/Nima.

- Lightweight as in 65Kb library + generated source code
- Full use of Javalin or Helidon SE/Nima as desired


## Define a Controller (Note that these APT processors works with both Java and Kotlin.)
```java
package org.example.hello;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import java.util.List;

@Path("/widgets")
@Controller
public class WidgetController {
  private final HelloComponent hello;
  public WidgetController(HelloComponent hello) {
    this.hello = hello;
  }
  
  @Get("/{id}")
  Widget getById(int id) {
    return new Widget(id, "you got it"+ hello.hello());
  }

  @Get
  List<Widget> getAll() {
    return List.of(new Widget(1, "Rob"), new Widget(2, "Fi"));
  }

  record Widget(int id, String name){};
}

```

## Usage with Javalin

The annotation processor will generate controller classes implementing the WebRoutes interface, which means we can
get all the WebRoutes and register them with Javalin using:

```java
var routes = BeanScope.builder().build().list(WebRoutes.class); 

Javalin.create()
        .routes(() -> routes.forEach(WebRoutes::registerRoutes))
        .start();
```


## Usage with Helidon SE

The annotation processor will generate controller classes implementing the Helidon Service interface, which we can use
get all the Services and register them with Helidon `RoutingBuilder`.

```java
var routes = BeanScope.builder().build().list(Service.class); 
var routingBuilder = Routing.builder().register(routes.stream().toArray(Service[]::new));
WebServer.builder()
        .addMediaSupport(JacksonSupport.create())
        .routing(routingBuilder)
        .build()
        .start();
```

## Usage with Helidon Nima

The annotation processor will generate controller classes implementing the Helidon HttpService interface, which we can use
get all the services and register them with the Helidon `HttpRouting`.

```java
var routes = BeanScope.builder().build().list(HttpService.class); 
final var builder = HttpRouting.builder();

for (final HttpService httpService : routes) {
   httpService.routing(builder);
}

WebServer.builder()
         .addRouting(builder.build())
         .build()
         .start();
```
## Generated sources

### (Javalin) The generated WidgetController$Route.java is:

```java
package org.example.hello;

import static io.avaje.http.api.PathTypeConversion.*;
import io.avaje.http.api.WebRoutes;
import io.javalin.apibuilder.ApiBuilder;
import javax.annotation.Generated;
import javax.inject.Singleton;
import org.example.hello.WidgetController;

@Generated("io.avaje.javalin-generator")
@Singleton
public class WidgetController$Route implements WebRoutes {

 private final WidgetController controller;

 public WidgetController$route(WidgetController controller) {
   this.controller = controller;
 }

  @Override
  public void registerRoutes() {

    ApiBuilder.get("/widgets/{id}", ctx -> {
      int id = asInt(ctx.pathParam("id"));
      ctx.json(controller.getById(id));
      ctx.status(200);
    });

    ApiBuilder.get("/widgets", ctx -> {
      ctx.json(controller.getAll());
      ctx.status(200);
    });

  }
}
```

### (Helidon SE) The generated WidgetController$Route.java is:
```java
package org.example.hello;

import static io.avaje.http.api.PathTypeConversion.*;

import io.avaje.http.api.*;
import io.helidon.common.http.FormParams;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import jakarta.inject.Singleton;
import org.example.hello.WidgetController;

@Generated("io.dinject.helidon-generator")
@Singleton
public class WidgetController$Route implements Service {

  private final WidgetController controller;

  public WidgetController$Route(WidgetController controller) {
    this.controller = controller;
  }

  @Override
  public void update(Routing.Rules rules) {

    rules.get("/widgets/{id}", this::_getById);
    rules.post("/widgets", this::_getAll);
  }

  private void _getById(ServerRequest req, ServerResponse res) {
    int id = asInt(req.path().param("id"));
    res.send(controller.getById(id));
  }

  private void _getAll(ServerRequest req, ServerResponse res) {
    res.send(controller.getAll());
  }

}
```

### (Helidon Nima) The generated WidgetController$Route.java is:

```java
package org.example.hello;

import static io.avaje.http.api.PathTypeConversion.*;

import io.avaje.http.api.*;
import io.helidon.common.http.FormParams;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import jakarta.inject.Singleton;
import org.example.hello.WidgetController;

@Generated("io.dinject.helidon-generator")
@Singleton
public class WidgetController$Route implements Service {

  private final WidgetController controller;

  public WidgetController$Route(WidgetController controller) {
    this.controller = controller;
  }

  @Override
  public void update(Routing.Rules rules) {

    rules.get("/widgets/{id}", this::_getById);
    rules.post("/widgets", this::_getAll);
  }

  private void _getById(ServerRequest req, ServerResponse res) {
    int id = asInt(req.path().param("id"));
    res.send(controller.getById(id));
  }

  private void _getAll(ServerRequest req, ServerResponse res) {
    res.send(controller.getAll());
  }

}
```


