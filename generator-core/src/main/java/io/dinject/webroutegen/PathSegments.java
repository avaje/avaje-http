package io.dinject.webroutegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class PathSegments {

  static final PathSegments EMPTY = new PathSegments("", Collections.emptySet());

  static PathSegments parse(String fullPath) {

    Set<Segment> segments = new LinkedHashSet<>();

    StringBuilder path = new StringBuilder();

    if ("/".equals(fullPath)) {
      path.append("/");

    } else {
      for (String section : fullPath.split("/")) {
        if (!section.isEmpty()) {
          path.append("/");
          if (section.startsWith(":")) {
            Segment segment = createSegment(section.substring(1));
            segments.add(segment);
            path.append(segment.path(section));

          } else if ((section.startsWith("{") && (section.endsWith("}")))) {
            Segment segment = createSegment(section.substring(1, section.length() - 1));
            segments.add(segment);
            path.append(segment.path(section));

          } else {
            path.append(section);
          }
        }
      }
    }

    return new PathSegments(path.toString(), segments);
  }

  private static Segment createSegment(String val) {

    String[] metricSplit = val.split(";");
    if (metricSplit.length == 1) {
      return new Segment(metricSplit[0]);
    }

    Set<String> metrics = new HashSet<>(Arrays.asList(metricSplit).subList(1, metricSplit.length));
    return new Segment(metricSplit[0], metrics);
  }

  private final String fullPath;

  private final Set<Segment> segments;

  private final List<Segment> withMetrics = new ArrayList<>();

  private final Set<String> allNames = new HashSet<>();

  private PathSegments(String fullPath, Set<Segment> segments) {
    this.fullPath = fullPath;
    this.segments = segments;
    for (Segment segment : segments) {
      segment.addNames(allNames);
      if (segment.hasMetrics()) {
        withMetrics.add(segment);
      }
    }
  }


  boolean contains(String varName) {
    return allNames.contains(varName);
  }

  List<Segment> metricSegments() {
    return withMetrics;
  }

  Segment segment(String varName) {

    for (Segment segment : segments) {
      if (segment.isPathParameter(varName)) {
        return segment;
      }
    }
    return null;
  }

  String fullPath() {
    return fullPath;
  }

  static class Segment {

    private final String name;

    /**
     * Metric keys.
     */
    private final Set<String> metrics;

    /**
     * Variable names the metrics map to (Java method param names).
     */
    private final Set<String> metricVarNames;

    Segment(String name) {
      this.name = name;
      this.metrics = null;
      this.metricVarNames = null;
    }

    Segment(String name, Set<String> metrics) {
      this.name = name;
      this.metrics = metrics;
      this.metricVarNames = new HashSet<>();
      for (String key : metrics) {
        metricVarNames.add(combine(name, key));
      }
    }

    void addNames(Set<String> allNames) {
      allNames.add(name);
    }

    boolean hasMetrics() {
      return metrics != null && !metrics.isEmpty();
    }

    private String combine(String name, String key) {
      return name + Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    Set<String> metrics() {
      return metrics;
    }

    String name() {
      return name;
    }

    boolean isPathParameter(String varName) {
      return name.equals(varName) || (metrics != null && (metricVarNames.contains(varName) || metrics.contains(varName)));
    }

    /**
     * Reading the value from a segment (rather than directly from pathParam).
     */
    void writeGetVal(Append writer, String varName, PlatformAdapter platform) {
      if (!hasMetrics()) {
        platform.writeReadParameter(writer, ParamType.PATHPARAM, name);
      } else {
        // TODO: platform read segment handling ...
        writer.append("%s_segment.", name);
        if (name.equals(varName)) {
          writer.append("val()");
        } else {
          writer.append("metric(\"%s\")", metricKey(varName));
        }
      }
    }

    private String metricKey(String varName) {

      if (!varName.startsWith(name)) {
        return varName;
      }

      String key = varName.substring(name.length());
      return Character.toLowerCase(key.charAt(0)) + key.substring(1);
    }

    void writeCreateSegment(Append writer) {
      // TODO: platform read segment handling ...
      writer.append("      PathSegment %s_segment = PathSegment.of(ctx.pathParam(\"%s_segment\"));", name, name).eol();
    }

    boolean isRequired(String varName) {
      return name.equals(varName);
    }

    String path(String section) {
      if (!hasMetrics()) {
        return section;
      }
      // TODO: platform read segment handling ...=
      return ":" + name + "_segment";
    }
  }
}
