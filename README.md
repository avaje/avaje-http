# avaje-http

HTTP server and client libraries via code generation.

## Http Server

A jax-rs style controllers with annotations (`@Path`, `@Get` ...)
that is lightweight by using source code generation (annotation processors)
to generate adapter code for Javalin and Helidon SE/Nima.

- Lightweight as in 65Kb library + generated source code
- Full use of Javalin or Helidon SE/Nima as desired

## Add dependencies

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-api</artifactId>
  <version>${avaje.http.version}</version>
</dependency>
```
Add the generator module for your desired microframework as a annotation processor.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>${maven-compiler-plugin.version}</version>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>io.avaje</groupId>
            <artifactId>avaje-http-javalin-generator</artifactId>
            <version>${avaje.http.version}</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Define a Controller (These APT processors work with both Java and Kotlin.)
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

  @Get()
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
@Generated("avaje-javalin-generator")
@Component
public class WidgetController$Route implements WebRoutes {

  private final WidgetController controller;

  public WidgetController$Route(WidgetController controller) {
    this.controller = controller;
  }

  @Override
  public void registerRoutes() {

    ApiBuilder.get("/widgets/{id}", ctx -> {
      ctx.status(200);
      var id = asInt(ctx.pathParam("id"));
      var result = controller.getById(id);
      ctx.json(result);
    });

    ApiBuilder.get("/widgets", ctx -> {
      ctx.status(200);
      var result = controller.getAll();
      ctx.json(result);
    });

  }

}
```

### (Helidon SE) The generated WidgetController$Route.java is:
```java
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
@Generated("avaje-helidon-nima-generator")
@Component
public class WidgetController$Route implements HttpService {

  private final WidgetController controller;
  public WidgetController$Route(WidgetController controller) {
    this.controller = controller;
  }

  @Override
  public void routing(HttpRules rules) {
    rules.get("/widgets/{id}", this::_getById);
    rules.get("/widgets", this::_getAll);
  }

  private void _getById(ServerRequest req, ServerResponse res) {
    var pathParams = req.path().pathParameters();
    int id = asInt(pathParams.first("id").get());
    var result = controller.getById(id);
    res.send(result);
  }

  private void _getAll(ServerRequest req, ServerResponse res) {
    var pathParams = req.path().pathParameters();
    var result = controller.getAll();
    res.send(result);
  }

}
```

## Generated sources ([Avaje-Jsonb](https://github.com/avaje/avaje-jsonb))
If [Avaje-Jsonb](https://github.com/avaje/avaje-jsonb) is detected, http generators with support will use it for faster Json message processing.

### (Javalin) The generated WidgetController$Route.java is:
```java
@Generated("avaje-javalin-generator")
@Component
public class WidgetController$Route implements WebRoutes {

  private final WidgetController controller;
  private final JsonType<java.util.List<org.example.hello.WidgetController.Widget>> listWidgetJsonType;
  private final JsonType<org.example.hello.WidgetController.Widget> widgetJsonType;

  public WidgetController$Route(WidgetController controller, Jsonb jsonB) {
    this.controller = controller;
    this.listWidgetJsonType = jsonB.type(org.example.hello.WidgetController.Widget.class).list();
    this.widgetJsonType = jsonB.type(org.example.hello.WidgetController.Widget.class);
  }

  @Override
  public void registerRoutes() {

    ApiBuilder.get("/widgets/{id}", ctx -> {
      ctx.status(200);
      var id = asInt(ctx.pathParam("id"));
      var result = controller.getById(id);
      widgetJsonType.toJson(result, ctx.contentType("application/json").outputStream());
    });

    ApiBuilder.get("/widgets", ctx -> {
      ctx.status(200);
      var result = controller.getAll();
      listWidgetJsonType.toJson(result, ctx.contentType("application/json").outputStream());
    });

  }

}
```

### (Helidon Nima) The generated WidgetController$Route.java is:

```java
@Generated("avaje-helidon-nima-generator")
@Component
public class WidgetController$Route implements HttpService {


  private final WidgetController controller;
  private final JsonType<org.example.hello.WidgetController.Widget> widgetJsonType;
  private final JsonType<java.util.List<org.example.hello.WidgetController.Widget>> listWidgetJsonType;

  public WidgetController$Route(WidgetController controller, Jsonb jsonB) {
    this.controller = controller;
    this.widgetJsonType = jsonB.type(org.example.hello.WidgetController.Widget.class);
    this.listWidgetJsonType = jsonB.type(org.example.hello.WidgetController.Widget.class).list();
  }

  @Override
  public void routing(HttpRules rules) {
    rules.get("/widgets/{id}", this::_getById);
    rules.get("/widgets", this::_getAll);
  }

  private void _getById(ServerRequest req, ServerResponse res) {
    var pathParams = req.path().pathParameters();
    int id = asInt(pathParams.first("id").get());
    var result = controller.getById(id);
    res.headers().contentType(io.helidon.common.http.HttpMediaType.APPLICATION_JSON);
    widgetJsonType.toJson(result, res.outputStream());
  }

  private void _getAll(ServerRequest req, ServerResponse res) {
    var pathParams = req.path().pathParameters();
    var result = controller.getAll();
    res.headers().contentType(io.helidon.common.http.HttpMediaType.APPLICATION_JSON);
    listWidgetJsonType.toJson(result, res.outputStream());
  }

}
```
