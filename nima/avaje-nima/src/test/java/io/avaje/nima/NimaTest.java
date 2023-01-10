package io.avaje.nima;

import io.avaje.inject.BeanScope;
import io.helidon.common.http.Http;
import io.helidon.nima.webserver.http.*;
import org.junit.jupiter.api.Test;

import java.net.URI;

class NimaTest {

  public static void main(String[] args) {
    HttpRouting.Builder builder = HttpRouting.builder()

      .any("/*", (req, res) -> {
        System.out.println("before any /*");
        res.next();
      })
      .get("/redir", (req, res) -> {
        System.out.println("redir");
        res.status(Http.Status.MOVED_PERMANENTLY_301);
        res.headers().location(URI.create("/hi"));
        res.send();
        //res.send("hi");
        //res.next();
      })
      .get("/hi", (req, res) -> {
        System.out.println("hi");
        res.send("hi");
        //res.next();
      })
      .get("/foo/{+blah}", (req, res) -> {
        System.out.println("foo");
        String blah = req.path().pathParameters().first("blah").orElseThrow();
        res.send("foo blah=" + blah);
      })
      .get("/bar/{blah}", (req, res) -> {
        System.out.println("bar");
        String blah = req.path().pathParameters().first("blah").orElseThrow();
        res.send("bar blah=" + blah);
      })
      .any("/*", (req, res) -> {
        System.out.println("after any /*");
        res.next();
      })
      .addFilter((chain, req, res) -> {
        System.out.println("Filter Before path:" + req.path().path() + " raw:" + req.path().rawPath()+" query:" + req.query().rawValue());
        chain.proceed();
        System.out.println("Filter After" + req.path());
      }).addFilter((chain, req, res) -> {
        System.out.println("Filter222 Before path:" + req.path().path());
        chain.proceed();
        System.out.println("Filter222 After" + req.path());
      })
      ;
    BeanScope scope = BeanScope.builder()
      .bean(HttpRouting.Builder.class, builder)
      .build();

    Nima nima = new Nima();
    nima.configure(scope);

    nima.start(8082);
  }

  @Test
  void initTest() {

    //nima.port();

  }
}
