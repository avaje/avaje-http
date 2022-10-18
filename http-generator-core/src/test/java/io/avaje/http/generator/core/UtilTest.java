package io.avaje.http.generator.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTest {

  @Test
  void combinePath() {

    assertEquals(Util.combinePath("/hello", null), "/hello");

    assertEquals(Util.combinePath(null, "/hello"), "/hello");
    assertEquals(Util.combinePath(null, "/hello/"), "/hello");
    assertEquals(Util.combinePath(null, "hello"), "/hello");
    assertEquals(Util.combinePath(null, "hello/"), "/hello");

    assertEquals(Util.combinePath(null, "/hello/:id"), "/hello/:id");
    assertEquals(Util.combinePath(null, "/hello/:id/:gid"), "/hello/:id/:gid");

    assertEquals(Util.combinePath("/a", "/hello"), "/a/hello");
    assertEquals(Util.combinePath("/a/b", "/hello"), "/a/b/hello");
    assertEquals(Util.combinePath("/a/b", "/hello"), "/a/b/hello");
    assertEquals(Util.combinePath("/a", ""), "/a");
    assertEquals(Util.combinePath("/a", "/"), "/a");
  }

  @Test
  void combinePath_forRoot() {
    assertEquals(Util.combinePath("/", ""), "/");
    assertEquals(Util.combinePath("", "/"), "");
  }

  @Test
  void trimPath() {
    assertEquals(Util.trimPath("/"), "/");
    assertEquals(Util.trimPath("/foo"), "/foo");
    assertEquals(Util.trimPath("/foo/"), "/foo");
  }

  @Test
  void snakeCase() {

    assertThat(Util.snakeCase("lower")).isEqualTo("lower");
    assertThat(Util.snakeCase("fooId")).isEqualTo("foo-id");
    assertThat(Util.snakeCase("_fooId")).isEqualTo("_foo-id");
    assertThat(Util.snakeCase("fooBarBazUuid")).isEqualTo("foo-bar-baz-uuid");
    assertThat(Util.snakeCase("aDTo")).isEqualTo("a-d-to");
    assertThat(Util.snakeCase("DTo")).isEqualTo("d-to");
    assertThat(Util.snakeCase("_DTo")).isEqualTo("_-d-to");
  }

  @Test
  void initcap() {
    assertThat(Util.initcapSnake("lower")).isEqualTo("Lower");
    assertThat(Util.initcapSnake("a")).isEqualTo("A");
    assertThat(Util.initcapSnake("myFoo")).isEqualTo("MyFoo");
  }

  @Test
  void initcapSnake() {
    assertThat(Util.initcapSnake("lower")).isEqualTo("Lower");
    assertThat(Util.initcapSnake("foo-id")).isEqualTo("Foo-Id");
    assertThat(Util.initcapSnake("foo-bar-baz-uuid")).isEqualTo("Foo-Bar-Baz-Uuid");
    assertThat(Util.initcapSnake("a-d-to")).isEqualTo("A-D-To");
    assertThat(Util.initcapSnake("proxy-authenticate")).isEqualTo("Proxy-Authenticate");
  }

  @Test
  void propertyName() {
    assertThat(Util.propertyName("setLower")).isEqualTo("lower");
    assertThat(Util.propertyName("setFooBar")).isEqualTo("fooBar");
  }

  @Test
  void parse_basic() {
    UType type = Util.parse("org.example.Repo");

    assertThat(type.importTypes()).containsExactly("org.example.Repo");
    assertThat(type.shortType()).isEqualTo("Repo");
    assertThat(type.shortName()).isEqualTo("repo");
  }

  @Test
  void parse_generic() {
    UType type = Util.parse("java.util.List<org.example.Repo>");

    assertThat(type.importTypes()).containsExactly("java.util.List", "org.example.Repo");
    assertThat(type.shortType()).isEqualTo("List<Repo>");
    assertThat(type.shortName()).isEqualTo("listRepo");
  }

  @Test
  void parse_generic_twoParams() {
    UType type = Util.parse("java.util.List<org.example.Repo, foo.Other>");

    assertThat(type.importTypes()).containsExactly("java.util.List", "org.example.Repo", "foo.Other");
    assertThat(type.shortType()).isEqualTo("List<Repo,Other>");
  }

  @Test
  void parse_CompletableFutureHttpVoid() {
    UType type = Util.parse("java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<java.lang.Void>>");

    assertThat(type.importTypes()).containsExactly("java.util.concurrent.CompletableFuture", "java.net.http.HttpResponse");
    assertThat(type.shortType()).isEqualTo("CompletableFuture<HttpResponse<Void>>");
  }

  @Test
  void parse_CompletableFutureBean() {
    UType type = Util.parse("java.util.concurrent.CompletableFuture<org.example.Repo>");

    assertThat(type.importTypes()).containsExactly("java.util.concurrent.CompletableFuture", "org.example.Repo");
    assertThat(type.shortType()).isEqualTo("CompletableFuture<Repo>");
  }

  @Test
  void parse_CompletableFutureListBean() {
    UType type = Util.parse("java.util.concurrent.CompletableFuture<java.util.List<org.example.Repo>>");

    assertThat(type.importTypes()).containsExactly("java.util.concurrent.CompletableFuture", "java.util.List", "org.example.Repo");
    assertThat(type.shortType()).isEqualTo("CompletableFuture<List<Repo>>");
  }

  @Test
  void parse_CompletableFutureStreamBean() {
    UType type = Util.parse("java.util.concurrent.CompletableFuture<java.util.Stream<org.example.Repo>>");

    assertThat(type.importTypes()).containsExactly("java.util.concurrent.CompletableFuture", "java.util.Stream", "org.example.Repo");
    assertThat(type.shortType()).isEqualTo("CompletableFuture<Stream<Repo>>");
    assertThat(type.shortName()).isEqualTo("completableFutureStreamRepo");
  }

  @Test
  void parse_BodyHandler_E() {
    UType type = Util.parse("java.net.http.HttpResponse.BodyHandler<E>");

    assertThat(type.importTypes()).containsExactly("java.net.http.HttpResponse.BodyHandler");
    assertThat(type.shortType()).isEqualTo("BodyHandler<E>");
    assertThat(type.genericParams()).isEqualTo("<E> ");
  }

  @Test
  void parse_BodyHandler_Path() {
    UType type = Util.parse("java.net.http.HttpResponse.BodyHandler<java.util.Path>");

    assertThat(type.importTypes()).containsExactly("java.net.http.HttpResponse.BodyHandler", "java.util.Path");
    assertThat(type.shortType()).isEqualTo("BodyHandler<Path>");
    assertThat(type.shortName()).isEqualTo("bodyHandlerPath");
    assertThat(type.genericParams()).isEqualTo("");
  }

  @Test
  void parse_BodyHandler_Multi() {
    UType type = Util.parse("java.net.http.HttpResponse.BodyHandler<some.Foo<A,B>>");

    assertThat(type.importTypes()).containsExactly("java.net.http.HttpResponse.BodyHandler", "some.Foo");
    assertThat(type.shortType()).isEqualTo("BodyHandler<Foo<A,B>>");
    assertThat(type.shortName()).isEqualTo("bodyHandlerFooAB");
    assertThat(type.genericParams()).isEqualTo("<A,B> ");
  }

  @Test
  void parse_BodyHandler_Multi2() {
    UType type = Util.parse("java.net.http.HttpResponse.BodyHandler<some.Foo<AB,BC,some.Bar<D>>>");

    assertThat(type.importTypes()).containsExactly("java.net.http.HttpResponse.BodyHandler", "some.Foo", "some.Bar");
    assertThat(type.shortType()).isEqualTo("BodyHandler<Foo<AB,BC,Bar<D>>>");
    assertThat(type.shortName()).isEqualTo("bodyHandlerFooABBCBarD");
    assertThat(type.genericParams()).isEqualTo("<AB,BC,D> ");
  }

  @Test
  void utypeShortName() {
    UType type = Util.parse("java.util.Map<java.util.String,org.foo.Person>");
    assertThat(type.shortName()).isEqualTo("mapStringPerson");
    assertThat(type.shortType()).isEqualTo("Map<String,Person>");
  }

  @Test
  void shortName() {
    assertThat(Util.name("List<Person>")).isEqualTo("listPerson");
    assertThat(Util.name("Set<Person>")).isEqualTo("setPerson");
    assertThat(Util.name("Map<String,Person>")).isEqualTo("mapStringPerson");
  }

}
