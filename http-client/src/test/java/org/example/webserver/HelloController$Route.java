package org.example.webserver;

import static io.avaje.http.api.PathTypeConversion.asInt;
import static io.avaje.http.api.PathTypeConversion.asLocalDate;
import static io.avaje.http.api.PathTypeConversion.toLocalDate;

import java.time.LocalDate;

import io.avaje.http.api.AvajeJavalinPlugin;
import io.avaje.http.api.PathSegment;
import io.avaje.http.api.Validator;
import io.javalin.config.JavalinConfig;
import io.javalin.router.JavalinDefaultRouting;

//@Singleton
public class HelloController$Route extends AvajeJavalinPlugin {

  private final HelloController controller;
  private final Validator validator;

  public HelloController$Route(HelloController controller, Validator validator) {
   this.controller = controller;
   this.validator = validator;
  }


  @Override
  public void onStart(JavalinConfig cfg) {
    cfg.router.mount(this::routes);
  }

  private void routes(JavalinDefaultRouting cfg) {

	  cfg.get("/hello/message", ctx -> {
      ctx.status(200);
      ctx.contentType("text/plain").result(controller.getPlainMessage());
    });

	  cfg.get("/hello/retry", ctx -> {
      ctx.status(200);
      ctx.contentType("text/plain").result(controller.retry());
    });

	  cfg.get("/hello/basicAuth", ctx -> {
      ctx.status(200);
      final String authorization = ctx.header("Authorization");
      ctx.result(controller.basicAuth(authorization));
    });

    cfg.get("/hello/stream", ctx -> {
      ctx.status(200);
      controller.stream(ctx);
    });

    cfg.get("/hello/{id}/{date}", ctx -> {
      ctx.status(200);
      final int id = asInt(ctx.pathParam("id"));
      final LocalDate date = asLocalDate(ctx.pathParam("date"));
      final String otherParam = ctx.queryParam("otherParam");
      ctx.json(controller.hello(id, date, otherParam));
    });

    cfg.get("/hello/findbyname/{name}", ctx -> {
      ctx.status(200);
      final String name = ctx.pathParam("name");
      final String otherParam = ctx.queryParam("otherParam");
      ctx.json(controller.findByName(name, otherParam));
    });

    cfg.post("/hello", ctx -> {
      ctx.status(201);
      final HelloDto dto = ctx.bodyAsClass(HelloDto.class);
      validator.validate(dto, "en-us");
      ctx.json(controller.post(dto));
    });

    cfg.post("/hello/savebean/{foo}", ctx -> {
      ctx.status(201);
      final String foo = ctx.pathParam("foo");
      final HelloDto dto = ctx.bodyAsClass(HelloDto.class);
      validator.validate(dto, "en-us");
      controller.saveBean(foo, dto, ctx);
    });

    cfg.post("/hello/saveform", ctx -> {
      ctx.status(201);
      final HelloForm helloForm =  new HelloForm(
        ctx.formParam("name"),
        ctx.formParam("email")
      );
      helloForm.url = ctx.formParam("url");
      helloForm.startDate = toLocalDate(ctx.formParam("startDate"));

      validator.validate(helloForm, "en-us");
      controller.saveForm(helloForm);
    });

    cfg.post("/hello/saveform2", ctx -> {
      ctx.status(201);
      final String name = ctx.formParam("name");
      final String email = ctx.formParam("email");
      final String url = ctx.formParam("url");
      controller.saveForm2(name, email, url);
    });

    cfg.post("/hello/saveform3", ctx -> {
      ctx.status(201);
      final HelloForm helloForm =  new HelloForm(
        ctx.formParam("name"),
        ctx.formParam("email")
      );
      helloForm.url = ctx.formParam("url");
      helloForm.startDate = toLocalDate(ctx.formParam("startDate"));

      validator.validate(helloForm, "en-us");
      ctx.json(controller.saveForm3(helloForm));
    });

    cfg.get("/hello", ctx -> {
      ctx.status(200);
      ctx.json(controller.getAll());
    });

    cfg.delete("/hello/{id}", ctx -> {
      ctx.status(204);
      final int id = asInt(ctx.pathParam("id"));
      controller.deleteById(id);
    });

    cfg.get("/hello/withMatrix/{year_segment}/{other}", ctx -> {
      ctx.status(200);
      final PathSegment year_segment = PathSegment.of(ctx.pathParam("year_segment"));
      final int year = asInt(year_segment.val());
      final String author = year_segment.matrix("author");
      final String country = year_segment.matrix("country");
      final String zone = year_segment.matrix("zone");
      final String other = ctx.pathParam("other");
      final String extra = ctx.queryParam("extra");
      ctx.contentType("text/plain").result(controller.getWithMatrixParam(year, author, country, zone, other, extra));
    });

  }

}
