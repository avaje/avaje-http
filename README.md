# [Avaje-HTTP](https://avaje.io/http/)
[![Build](https://github.com/avaje/avaje-http/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-http/actions/workflows/build.yml)
[![Maven Central : avaje-inject](https://img.shields.io/maven-central/v/io.avaje/avaje-http-api.svg?label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-http-api)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-inject/blob/master/LICENSE)
[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)

HTTP server and client libraries via code generation.

## [HTTP Client](https://avaje.io/http-client/)

A light (~80kb) wrapper to the [JDK 11+ Java Http Client](http://openjdk.java.net/groups/net/httpclient/intro.html). Additionally, you can create Feign-style interfaces and have implementations generated via annotation processing.

- Fluid API for building URLs and payload
- JSON marshaling using Avaje Jsonb/Jackson/Gson
- Light Feign-style interfaces via annotation processing.
- Request/Response Interception
- Authorization via Basic Auth or OAuth Bearer Tokens
- Async and sync API

## [HTTP Server](https://avaje.io/http/)

Use source code generation to adapt annotated REST controllers `@Path, @Get, @Post, etc` to Javalin, Helidon SE, and similar web routing HTTP servers.

- Lightweight (65Kb library + generated source code)
- Full use of Javalin or Helidon SE/Nima as desired
- Bean Validation of request bodies supported (validation groups supported as well)

## Add dependencies
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-api</artifactId>
  <version>${avaje.http.version}</version>
</dependency>
```
**Add the generator module for your desired microframework as an annotation processor.**

```xml
<!-- Annotation processors -->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-{javalin/helidon}-generator</artifactId>
  <version>${avaje-http.version}</version>
  <scope>provided</scope>
</dependency>
```

### JDK 22+

In JDK 22+, annotation processors are disabled by default, you will need to add a flag to re-enable.
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <compilerArgument>-proc:full</compilerArgument>
  </configuration>
</plugin>
```

## Define a Controller (These APT processors work with both Java and Kotlin)
```java
package org.example.hello;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import java.util.List;

@Controller("/widgets")
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
## DI Usage
The annotation processor will generate controller adapters to register routes to Javalin/Helidon. The natural way to use the generated adapters is to get a DI library to find and wire them. The AP will automatically detect the presence of avaje-inject and generate the class to use avaje-inject's `@Component` as the DI annotation.

There isn't a hard requirement to use [Avaje](https://avaje.io/inject/) for dependency injection. In the absence of avaje-inject, the generated class will use `@jakarta.inject.Singleton` or `@javax.inject.Singleton` depending on what's on the classpath. Any DI library that can find and wire the generated @Singleton beans can be used. You can even use Dagger2 or Guice to wire the controllers if you so desire.

To force the AP to generate with `@javax.inject.Singleton`(in the case where you have both jakarta and javax on the classpath), use the compiler arg `-AuseJavax=true`
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <compilerArgs>
      <arg>-AuseJavax=true</arg>
    </compilerArgs>
  </configuration>
</plugin>
```

### Usage with Javalin

The annotation processor will generate controller classes implementing the Javalin `Plugin` interface, which we can register using:

```java
List<Plugin> routes = ...; //retrieve using a DI framework

Javalin.create(cfg -> routes.forEach(cfg.plugins::register)).start();
```

### Usage with Helidon SE (4.x)

The annotation processor will generate controller classes implementing the Helidon HttpFeature interface, which we can register with the Helidon `HttpRouting`.

```java
List<HttpFeature> routes = ... //retrieve using a DI framework
final var builder = HttpRouting.builder();

routes.forEach(builder::addFeature);

WebServer.builder()
         .addRouting(builder)
         .build()
         .start();
```
## Generated sources

### (Javalin) The generated WidgetController$Route.java is:

```java
@Generated("avaje-javalin-generator")
@Singleton
public class WidgetController$Route implements Plugin {

  private final WidgetController controller;

  public WidgetController$Route(WidgetController controller) {
    this.controller = controller;
  }

  @Override
  public void apply(Javalin app) {

    app.get("/widgets/{id}", ctx -> {
      ctx.status(200);
      var id = asInt(ctx.pathParam("id"));
      var result = controller.getById(id);
      ctx.json(result);
    });

    app.get("/widgets", ctx -> {
      ctx.status(200);
      var result = controller.getAll();
      ctx.json(result);
    });

  }
}
```

### (Helidon SE) The generated WidgetController$Route.java is:

```java
@Generated("avaje-helidon-generator")
@Component
public class WidgetController$Route implements HttpFeature {

  private final WidgetController controller;

  public WidgetController$Route(WidgetController controller) {
    this.controller = controller;
  }

  @Override
  public void setup(HttpRouting.Builder routing) {
    routing.get("/widgets/{id}", this::_getById);
    routing.get("/widgets", this::_getAll);
  }

  private void _getById(ServerRequest req, ServerResponse res) throws Exception {
    res.status(OK_200);
    var pathParams = req.path().pathParameters();
    var id = asInt(pathParams.first("id").get());
    var result = controller.getById(id);
    res.send(result);
  }

  private void _getAll(ServerRequest req, ServerResponse res) throws Exception {
    res.status(OK_200);
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
public class WidgetController$Route implements Plugin {

  private final WidgetController controller;
  private final JsonType<List<Widget>> listWidgetJsonType;
  private final JsonType<Widget> widgetJsonType;

  public WidgetController$Route(WidgetController controller, Jsonb jsonB) {
    this.controller = controller;
    this.listWidgetJsonType = jsonB.type(Widget.class).list();
    this.widgetJsonType = jsonB.type(Widget.class);
  }

  @Override
  public void apply(Javalin app) {

    app.get("/widgets/{id}", ctx -> {
      ctx.status(200);
      var id = asInt(ctx.pathParam("id"));
      var result = controller.getById(id);
      widgetJsonType.toJson(result, ctx.contentType("application/json").outputStream());
    });

    app.get("/widgets", ctx -> {
      ctx.status(200);
      var result = controller.getAll();
      listWidgetJsonType.toJson(result, ctx.contentType("application/json").outputStream());
    });

  }
}
```

### (Helidon SE) The generated WidgetController$Route.java is:

```java
@Generated("avaje-helidon-generator")
@Component
public class WidgetController$Route implements HttpFeature {

  private final WidgetController controller;
  private final JsonType<WidgetController.Widget> widgetController$WidgetJsonType;
  private final JsonType<List<WidgetController.Widget>> listWidgetController$WidgetJsonType;

  public WidgetController$Route(WidgetController controller, Jsonb jsonb) {
    this.controller = controller;
    this.widgetController$WidgetJsonType = jsonb.type(WidgetController.Widget.class);
    this.listWidgetController$WidgetJsonType = jsonb.type(WidgetController.Widget.class).list();
  }

  @Override
  public void setup(HttpRouting.Builder routing) {
    routing.get("/widgets/{id}", this::_getById);
    routing.get("/widgets", this::_getAll);
  }

  private void _getById(ServerRequest req, ServerResponse res) throws Exception {
    res.status(OK_200);
    var pathParams = req.path().pathParameters();
    var id = asInt(pathParams.first("id").get());
    var result = controller.getById(id);
    res.headers().contentType(MediaTypes.APPLICATION_JSON);
    //jsonb has a special accommodation for helidon to improve performance
    widgetController$WidgetJsonType.toJson(result, JsonOutput.of(res));
  }

  private void _getAll(ServerRequest req, ServerResponse res) throws Exception {
    res.status(OK_200);
    var result = controller.getAll();
    res.headers().contentType(MediaTypes.APPLICATION_JSON);
    listWidgetController$WidgetJsonType.toJson(result, JsonOutput.of(res));
  }

}
```
