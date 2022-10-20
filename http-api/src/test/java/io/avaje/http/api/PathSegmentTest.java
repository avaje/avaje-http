package io.avaje.http.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class PathSegmentTest {

  @Test
  public void simple() {

    PathSegment seg = PathSegment.of("simple");

    assertEquals("simple", seg.val());
    assertNull(seg.matrix("foo"));
  }

  @Test
  public void singleMetric() {

    PathSegment seg = new PathSegment("simple;k=v");

    assertEquals("simple", seg.val());
    assertEquals("v", seg.matrix("k"));
    assertNull(seg.matrix("foo"));
  }

  @Test
  public void singleMultiMetric() {

    PathSegment seg = PathSegment.of("simple;k=v;l=m");

    assertEquals("simple", seg.val());
    assertEquals("v", seg.matrix("k"));
    assertEquals("m", seg.matrix("l"));
    assertNull(seg.matrix("foo"));
  }

  @Test
  public void emptyStringMetrics() {

    PathSegment seg = new PathSegment("simple;k=;l=");

    assertEquals("simple", seg.val());
    assertNull(seg.matrix("k"));
    assertNull(seg.matrix("l"));
    assertNull(seg.matrix("foo"));
  }
}
