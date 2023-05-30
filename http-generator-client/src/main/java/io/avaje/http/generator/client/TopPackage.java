package io.avaje.http.generator.client;

import java.util.Collection;

final class TopPackage {

  private String topPackage;

  static String of(Collection<String> values) {
    return new TopPackage(values).value();
  }

  private String value() {
    return topPackage;
  }

  private TopPackage(Collection<String> values) {
    for (final String pkg : values) {
      topPackage = commonParent(topPackage, pkg);
    }
  }

  /** Return the common parent package. */
  static String commonParent(String currentTop, String aPackage) {
    if (aPackage == null) return currentTop;
    if (currentTop == null) return packageOf(aPackage);
    if (aPackage.startsWith(currentTop)) {
      return currentTop;
    }
    int next;
    do {
      next = currentTop.lastIndexOf('.');
      if (next > -1) {
        currentTop = currentTop.substring(0, next);
        if (aPackage.startsWith(currentTop)) {
          return currentTop;
        }
      }
    } while (next > -1);

    return currentTop;
  }

  static String packageOf(String cls) {
    final int pos = cls.lastIndexOf('.');
    return (pos == -1) ? "" : cls.substring(0, pos);
  }
}
