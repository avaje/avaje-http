package io.avaje.http.generator.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PathSegmentsTest {

  @Test
  public void parse_standard() {

    PathSegments segments = PathSegments.parse("/before/{one}/{two}/after");

    assertTrue(segments.contains("one"));
    assertTrue(segments.contains("two"));
    assertFalse(segments.contains("before"));
    assertFalse(segments.contains("after"));

    segments = PathSegments.parse("/{one}/{two}");

    assertTrue(segments.contains("one"));
    assertTrue(segments.contains("two"));
  }

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
  public void pathMatrixParams_colonPrefix() {

    PathSegments segments = PathSegments.parse("/:id;key;other/:foo;baz");

    List<PathSegments.Segment> matrixSegments = segments.matrixSegments();
    Assertions.assertThat(matrixSegments).hasSize(2);

    assertThat(matrixSegments.get(0).name()).isEqualTo("id");
    assertThat(matrixSegments.get(0).matrixKeys()).containsOnly("key", "other");

    assertThat(matrixSegments.get(1).name()).isEqualTo("foo");
    assertThat(matrixSegments.get(1).matrixKeys()).containsOnly("baz");

    assertEquals("/:id_segment/:foo_segment", segments.fullPath());
  }

  @Test
  public void pathMatrixParams_normalised() {

    PathSegments segments = PathSegments.parse("/{id;key;other}/{foo;baz}");

    List<PathSegments.Segment> matrixSegments = segments.matrixSegments();
    Assertions.assertThat(matrixSegments).hasSize(2);

    assertThat(matrixSegments.get(0).name()).isEqualTo("id");
    assertThat(matrixSegments.get(0).matrixKeys()).containsOnly("key", "other");

    assertThat(matrixSegments.get(1).name()).isEqualTo("foo");
    assertThat(matrixSegments.get(1).matrixKeys()).containsOnly("baz");

    assertEquals("/{id_segment}/{foo_segment}", segments.fullPath());
  }

  @Test
  public void pathMatrixParams_fullPath() {

    PathSegments segments = PathSegments.parse("/start/:id;key;other/:foo;baz/end");
    assertEquals("/start/:id_segment/:foo_segment/end", segments.fullPath());

    segments = PathSegments.parse("/start/:id;key;other/middle/:foo;baz/end");

    assertEquals("/start/:id_segment/middle/:foo_segment/end", segments.fullPath());
  }

  @Test
  public void pathMatrixParams_fullPath_normalised() {

    PathSegments segments = PathSegments.parse("/start/{id;key;other}/{foo;baz}/end");
    assertEquals("/start/{id_segment}/{foo_segment}/end", segments.fullPath());

    segments = PathSegments.parse("/start/{id;key;other}/middle/{foo;baz}/end");
    assertEquals("/start/{id_segment}/middle/{foo_segment}/end", segments.fullPath());
  }

}
