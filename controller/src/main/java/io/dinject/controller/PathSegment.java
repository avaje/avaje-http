package io.dinject.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * A path segment that can simple like value like <code>chair</code>
 * or contain matrix parameter values using semi-colon delimitation
 * like <code>chair;vendor=ikea;size=small</code>.
 * <p>
 * Matrix parameters are optional 'qualifiers' of the path segment.
 * </p>
 */
public class PathSegment {

  private final String val;

  private Map<String, String> metricValues;

  /**
   * Create with a given value that may contain matrix parameters.
   */
  public static PathSegment of(String value) {
    return new PathSegment(value);
  }

  /**
   * Create with a given value that may contain matric parameters.
   */
  public PathSegment(String value) {
    String[] vals = value.split(";");
    this.val = vals[0];
    if (vals.length > 1) {
      metricValues = new HashMap<>();
      for (String val : vals) {
        String[] keyVal = val.split("=");
        if (keyVal.length == 2) {
          metricValues.put(keyVal[0], keyVal[1]);
        }
      }
    }
  }

  /**
   * Return the main segment value.
   * <p>
   * For "chair" this returns "chair"
   * </p>
   * <p>
   * For "chair;vendor=ikea;size=small" this returns "chair"
   * </p>
   */
  public String val() {
    return val;
  }

  /**
   * Return a metric value for the given key.
   * <p>
   * For example, given "chair;vendor=ikea;size=small"
   * </p>
   * <p>
   * metric("vendor") returns "ikea".
   * </p>
   *
   * @param key The metric key
   * @return The metric value if supplied or null
   */
  public String metric(String key) {
    return metricValues == null ? null : metricValues.get(key);
  }

}
