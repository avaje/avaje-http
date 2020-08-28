package io.avaje.http.generator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PathSegments {

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
            path.append(segment.path(section, ":", ""));

          } else if ((section.startsWith("{") && (section.endsWith("}")))) {
            Segment segment = createSegment(section.substring(1, section.length() - 1));
            segments.add(segment);
            path.append(segment.path(section, "{", "}"));

          } else {
            path.append(section);
          }
        }
      }
    }

    return new PathSegments(path.toString(), segments);
  }

  private static Segment createSegment(String val) {
    String[] matrixSplit = val.split(";");
    if (matrixSplit.length == 1) {
      return new Segment(matrixSplit[0]);
    }
    Set<String> matrixKeys = new HashSet<>(Arrays.asList(matrixSplit).subList(1, matrixSplit.length));
    return new Segment(matrixSplit[0], matrixKeys);
  }

  private final String fullPath;

  private final Set<Segment> segments;

  private final List<Segment> withMatrixs = new ArrayList<>();

  private final Set<String> allNames = new HashSet<>();

  private PathSegments(String fullPath, Set<Segment> segments) {
    this.fullPath = fullPath;
    this.segments = segments;
    for (Segment segment : segments) {
      segment.addNames(allNames);
      if (segment.hasMatrixParams()) {
        withMatrixs.add(segment);
      }
    }
  }


  public boolean contains(String varName) {
    return allNames.contains(varName);
  }

  public List<Segment> matrixSegments() {
    return withMatrixs;
  }

  public Segment segment(String varName) {

    for (Segment segment : segments) {
      if (segment.isPathParameter(varName)) {
        return segment;
      }
    }
    return null;
  }

  public String fullPath() {
    return fullPath;
  }

  public static class Segment {

    private final String name;

    /**
     * Matrix keys.
     */
    private final Set<String> matrixKeys;

    /**
     * Variable names the matrix map to (Java method param names).
     */
    private final Set<String> matrixVarNames;

    Segment(String name) {
      this.name = name;
      this.matrixKeys = null;
      this.matrixVarNames = null;
    }

    Segment(String name, Set<String> matrixKeys) {
      this.name = name;
      this.matrixKeys = matrixKeys;
      this.matrixVarNames = new HashSet<>();
      for (String key : matrixKeys) {
        matrixVarNames.add(combine(name, key));
      }
    }

    void addNames(Set<String> allNames) {
      allNames.add(name);
    }

    boolean hasMatrixParams() {
      return matrixKeys != null && !matrixKeys.isEmpty();
    }

    private String combine(String name, String key) {
      return name + Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    Set<String> matrixKeys() {
      return matrixKeys;
    }

    String name() {
      return name;
    }

    boolean isPathParameter(String varName) {
      return name.equals(varName) || (matrixKeys != null && (matrixVarNames.contains(varName) || matrixKeys.contains(varName)));
    }

    /**
     * Reading the value from a segment (rather than directly from pathParam).
     */
    void writeGetVal(Append writer, String varName, PlatformAdapter platform) {
      if (!hasMatrixParams()) {
        platform.writeReadParameter(writer, ParamType.PATHPARAM, name);
      } else {
        writer.append("%s_segment.", name);
        if (name.equals(varName)) {
          writer.append("val()");
        } else {
          writer.append("matrix(\"%s\")", matrixKey(varName));
        }
      }
    }

    private String matrixKey(String varName) {
      if (!varName.startsWith(name)) {
        return varName;
      }
      String key = varName.substring(name.length());
      return Character.toLowerCase(key.charAt(0)) + key.substring(1);
    }

    public void writeCreateSegment(Append writer, PlatformAdapter platform) {
      writer.append(platform.indent());
      writer.append("  PathSegment %s_segment = PathSegment.of(", name);
      platform.writeReadParameter(writer, ParamType.PATHPARAM, name + "_segment");
      writer.append(");").eol();
    }

    boolean isRequired(String varName) {
      return name.equals(varName);
    }

    String path(String section, String prefix, String suffix) {
      if (!hasMatrixParams()) {
        return section;
      }
      return prefix + name + "_segment" + suffix;
    }
  }
}
