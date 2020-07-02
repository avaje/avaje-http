package io.dinject.webroutegen;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class PathSegmentsTest {

  @Test
  public void parse_basic() {

    PathSegments segments = PathSegments.parse("/before/:one/:two/after");

    assertTrue(segments.contains("one"));
    assertTrue(segments.contains("two"));
    assertFalse(segments.contains("before"));
    assertFalse(segments.contains("after"));

    segments = PathSegments.parse("/:one/:two");

    assertTrue(segments.contains("one"));
    assertTrue(segments.contains("two"));
  }

  @Test
  public void parse_snake() {

    PathSegments segments = PathSegments.parse("/fo/:one-a/:two-b");

    assertTrue(segments.contains("one-a"));
    assertTrue(segments.contains("two-b"));

    assertEquals("/fo/:one-a/:two-b", segments.fullPath());

  }

  @Test
  public void parse_empty() {

    PathSegments segments = PathSegments.parse("/hello");
    assertFalse(segments.contains("hello"));
    assertEquals("/hello", segments.fullPath());

  }

  @Test
  public void pathParams() {

    PathSegments segments = PathSegments.parse("/:id;key;other/:foo;baz");

    List<PathSegments.Segment> metricSegments = segments.metricSegments();
    assertThat(metricSegments).hasSize(2);

    assertThat(metricSegments.get(0).name()).isEqualTo("id");
    assertThat(metricSegments.get(0).metrics()).containsOnly("key", "other");

    assertThat(metricSegments.get(1).name()).isEqualTo("foo");
    assertThat(metricSegments.get(1).metrics()).containsOnly("baz");

    assertEquals("/:id_segment/:foo_segment", segments.fullPath());
  }

  @Test
  public void pathParams_fullPath() {

    PathSegments segments = PathSegments.parse("/start/:id;key;other/:foo;baz/end");
    assertEquals("/start/:id_segment/:foo_segment/end", segments.fullPath());

    segments = PathSegments.parse("/start/:id;key;other/middle/:foo;baz/end");

    assertEquals("/start/:id_segment/middle/:foo_segment/end", segments.fullPath());
  }
}
