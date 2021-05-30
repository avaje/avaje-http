# avaje-http

Http server and client libraries and code generation.

## http server

A jax-rs style controllers with annotations (`@Path`, `@Get` ...)
that is lightweight by using source code generation (annotation processors)
to generate adapter code for Javalin and Helidon SE.

- Lightweight as in 65Kb library + generated source code
- Full use of Javalin or Helidon SE as desired


## Define a Controller
```java
package org.example.hello

import io.avaje.http.api.Controller
import io.avaje.http.api.Get
import io.avaje.http.api.Path

@Path("/widgets")
@Controller
class WidgetController(private val hello: HelloComponent) {

  @Get("/:id")
  fun getById(id : Int): Widget {
    return Widget(id, "you got it${hello.hello()}")
  }

  @Get
  fun getAll(): MutableList<Widget> {

    val list = mutableListOf<Widget>()
    list.add(Widget(1, "Rob"))
    list.add(Widget(2, "Fi"))

    return list
  }

  data class Widget(var id: Int, var name: String)
}

```

## Generated source

The annotation processor will generate a `$Route` for the controller like below.

Note that this class implements the WebRoutes interface, which means we can
get all the WebRoutes and register them with Javalin using.

```java
fun main(args: Array<String>) {

  // get all the webRoutes
  val webRoutes = SystemContext.getBeans(WebRoutes::class.java)

  val javalin = Javalin.create()

  javalin.routes {
    // register all the routes with Javalin
    webRoutes.forEach { it.registerRoutes() }

    // other routes etc as desired
    ApiBuilder.get("/foo") { ctx ->
      ctx.html("bar")
      ctx.status(200)
    }
    ...
  }

  javalin.start(7000)
}

```

### The generated WidgetController$Route.java is:


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

    ApiBuilder.get("/widgets/:id", ctx -> {
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

Note that this APT processor works with both Java and Kotlin.


