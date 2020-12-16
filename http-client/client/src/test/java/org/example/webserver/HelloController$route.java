package org.example.webserver;

import io.avaje.http.api.PathSegment;
import io.avaje.http.api.Validator;
import io.avaje.http.api.WebRoutes;
import io.javalin.apibuilder.ApiBuilder;

import jakarta.inject.Singleton;
import java.time.LocalDate;

import static io.avaje.http.api.PathTypeConversion.asInt;
import static io.avaje.http.api.PathTypeConversion.asLocalDate;
import static io.avaje.http.api.PathTypeConversion.toLocalDate;

@Singleton
public class HelloController$route implements WebRoutes {

  private final HelloController controller;
  private final Validator validator;

  public HelloController$route(HelloController controller, Validator validator) {
   this.controller = controller;
   this.validator = validator;
  }

  @Override
  public void registerRoutes() {

    ApiBuilder.get("/hello/message", ctx -> {
      ctx.status(200);
      ctx.contentType("text/plain").result(controller.getPlainMessage());
    });

    ApiBuilder.get("/hello/:id/:date", ctx -> {
      ctx.status(200);
      int id = asInt(ctx.pathParam("id"));
      LocalDate date = asLocalDate(ctx.pathParam("date"));
      String otherParam = ctx.queryParam("otherParam");
      ctx.json(controller.hello(id, date, otherParam));
    });

    ApiBuilder.get("/hello/findbyname/:name", ctx -> {
      ctx.status(200);
      String name = ctx.pathParam("name");
      String otherParam = ctx.queryParam("otherParam");
      ctx.json(controller.findByName(name, otherParam));
    });

    ApiBuilder.post("/hello", ctx -> {
      ctx.status(201);
      HelloDto dto = ctx.bodyAsClass(HelloDto.class);
      validator.validate(dto);
      ctx.json(controller.post(dto));
    });

    ApiBuilder.post("/hello/savebean/:foo", ctx -> {
      ctx.status(201);
      String foo = ctx.pathParam("foo");
      HelloDto dto = ctx.bodyAsClass(HelloDto.class);
      validator.validate(dto);
      controller.saveBean(foo, dto, ctx);
    });

    ApiBuilder.post("/hello/saveform", ctx -> {
      ctx.status(201);
      HelloForm helloForm =  new HelloForm(
        ctx.formParam("name"),
        ctx.formParam("email")
      );
      helloForm.url = ctx.formParam("url");
      helloForm.startDate = toLocalDate(ctx.formParam("startDate"));

      validator.validate(helloForm);
      controller.saveForm(helloForm);
    });

    ApiBuilder.post("/hello/saveform2", ctx -> {
      ctx.status(201);
      String name = ctx.formParam("name");
      String email = ctx.formParam("email");
      String url = ctx.formParam("url");
      controller.saveForm2(name, email, url);
    });

    ApiBuilder.post("/hello/saveform3", ctx -> {
      ctx.status(201);
      HelloForm helloForm =  new HelloForm(
        ctx.formParam("name"),
        ctx.formParam("email")
      );
      helloForm.url = ctx.formParam("url");
      helloForm.startDate = toLocalDate(ctx.formParam("startDate"));

      validator.validate(helloForm);
      ctx.json(controller.saveForm3(helloForm));
    });

    ApiBuilder.get("/hello", ctx -> {
      ctx.status(200);
      ctx.json(controller.getAll());
    });

    ApiBuilder.delete("/hello/:id", ctx -> {
      ctx.status(204);
      int id = asInt(ctx.pathParam("id"));
      controller.deleteById(id);
    });

    ApiBuilder.get("/hello/withMatrix/:year_segment/:other", ctx -> {
      ctx.status(200);
      PathSegment year_segment = PathSegment.of(ctx.pathParam("year_segment"));
      int year = asInt(year_segment.val());
      String author = year_segment.matrix("author");
      String country = year_segment.matrix("country");
      String other = ctx.pathParam("other");
      String extra = ctx.queryParam("extra");
      ctx.contentType("text/plain").result(controller.getWithMatrixParam(year, author, country, other, extra));
    });

  }

}
