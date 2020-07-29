package io.dinject.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PathSegmentTest {

  @Test
  public void simple() {

    PathSegment seg = PathSegment.of("simple");

    assertEquals("simple", seg.val());
    assertNull(seg.metric("foo"));
  }


  @Test
  public void singleMetric() {

    PathSegment seg = new PathSegment("simple;k=v");

    assertEquals("simple", seg.val());
    assertEquals("v", seg.metric("k"));
    assertNull(seg.metric("foo"));
  }

  @Test
  public void singleMultiMetric() {

    PathSegment seg = PathSegment.of("simple;k=v;l=m");

    assertEquals("simple", seg.val());
    assertEquals("v", seg.metric("k"));
    assertEquals("m", seg.metric("l"));
    assertNull(seg.metric("foo"));
  }

  @Test
  public void emptyStringMetrics() {

    PathSegment seg = new PathSegment("simple;k=;l=");

    assertEquals("simple", seg.val());
    assertNull(seg.metric("k"));
    assertNull(seg.metric("l"));
    assertNull(seg.metric("foo"));
  }
}
