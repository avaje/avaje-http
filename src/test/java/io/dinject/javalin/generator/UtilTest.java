package io.dinject.javalin.generator;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;


public class UtilTest {

  @Test
  public void combinePath() {

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
  public void pathParams() {

    assertThat(Util.pathParams("/hello")).isEmpty();
    assertThat(Util.pathParams("/a/:id/:foo")).contains("id", "foo");
    assertThat(Util.pathParams("/:id/:foo")).contains("id", "foo");
    assertThat(Util.pathParams("/odd:id/:foo")).contains("foo");
    assertThat(Util.pathParams("/:id/odd:foo")).contains("id");

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
