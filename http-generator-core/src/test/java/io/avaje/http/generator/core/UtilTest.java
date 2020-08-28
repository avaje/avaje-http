package io.avaje.http.generator.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class UtilTest {

  @Test
  public void combinePath() {

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
  public void combinePath_forRoot() {
    assertEquals(Util.combinePath("/", ""), "/");
    assertEquals(Util.combinePath("", "/"), "");
  }

  @Test
  public void trimPath() {
    assertEquals(Util.trimPath("/"), "/");
    assertEquals(Util.trimPath("/foo"), "/foo");
    assertEquals(Util.trimPath("/foo/"), "/foo");
  }

  @Test
  public void snakeCase() {

    assertThat(Util.snakeCase("lower")).isEqualTo("lower");
    assertThat(Util.snakeCase("fooId")).isEqualTo("foo-id");
    assertThat(Util.snakeCase("_fooId")).isEqualTo("_foo-id");
    assertThat(Util.snakeCase("fooBarBazUuid")).isEqualTo("foo-bar-baz-uuid");
    assertThat(Util.snakeCase("aDTo")).isEqualTo("a-d-to");
    assertThat(Util.snakeCase("DTo")).isEqualTo("d-to");
    assertThat(Util.snakeCase("_DTo")).isEqualTo("_-d-to");
  }

  @Test
  public void initcapSnake() {

    assertThat(Util.initcapSnake("lower")).isEqualTo("Lower");
    assertThat(Util.initcapSnake("foo-id")).isEqualTo("Foo-Id");
    assertThat(Util.initcapSnake("foo-bar-baz-uuid")).isEqualTo("Foo-Bar-Baz-Uuid");
    assertThat(Util.initcapSnake("a-d-to")).isEqualTo("A-D-To");
    assertThat(Util.initcapSnake("proxy-authenticate")).isEqualTo("Proxy-Authenticate");
  }

  @Test
  public void propertyName() {
    assertThat(Util.propertyName("setLower")).isEqualTo("lower");
    assertThat(Util.propertyName("setFooBar")).isEqualTo("fooBar");
  }
}
