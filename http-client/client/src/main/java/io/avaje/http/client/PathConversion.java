package io.avaje.http.client;

/**
 * Helper methods to convert common types to String path values.
 */
public class PathConversion {

  /**
   * Convert to path.
   */
  public static String toPath(int val) {
    return Integer.toString(val);
  }

  /**
   * Convert to path.
   */
  public static String toPath(long val) {
    return Long.toString(val);
  }

  /**
   * Convert to path.
   */
  public static String toPath(boolean val) {
    return Boolean.toString(val);
  }

  /**
   * Convert to path.
   */
  public static String toPath(double val) {
    return Double.toString(val);
  }

  /**
   * Convert to path.
   */
  public static String toPath(float val) {
    return Float.toString(val);
  }

  /**
   * Convert to path.
   */
  public static String toPath(Object val) {
    return val.toString();
  }

}
