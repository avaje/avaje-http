package io.kanuka.web.javlin;

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
}
