package io.kanuka.web.javlin;

import java.util.LinkedHashSet;
import java.util.Set;

class Util {

  static String extractOptionalType(String rawType) {
    return rawType.substring(19, rawType.length() - 1);
  }

  static boolean isOptional(String rawType) {
    return rawType.startsWith("java.util.Optional<");
  }

  static String extractList(String rawType) {
    return rawType.substring(15, rawType.length() - 1);
  }

  static boolean isList(String rawType) {
    return rawType.startsWith("java.util.List<");
  }

  static boolean isProvider(String rawType) {
    return rawType.startsWith("javax.inject.Provider<");
  }

  static String extractProviderType(String rawType) {
    return rawType.substring(22, rawType.length() - 1);
  }

  /**
   * Return the common parent package.
   */
  static String commonParent(String currentTop, String aPackage) {

    if (aPackage == null) return currentTop;
    if (currentTop == null) return aPackage;
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

  static String trimTrailingSlash(String value) {
    if (value.endsWith("/")) {
      return value.substring(0, value.length() - 1);
    }
    return value;
  }

  static String combinePath(String beanPath, String webMethodPath) {
    StringBuilder sb = new StringBuilder();
    if (beanPath != null) {
      sb.append(beanPath);
    }
    if (!webMethodPath.isEmpty() && !webMethodPath.startsWith("/")) {
      sb.append("/");
    }
    sb.append(trimTrailingSlash(webMethodPath));
    return sb.toString();
  }

  static Set<String> pathParams(String fullPath) {

    Set<String> paramNames = new LinkedHashSet<>();
    for (String section : fullPath.split("/")) {
      if (section.startsWith(":")) {
        paramNames.add(section.substring(1));
      }
    }
    return paramNames;
  }

  static String packageOf(String cls) {
    int pos = cls.lastIndexOf('.');
    return (pos == -1) ? "" : cls.substring(0, pos);
  }

  static String shortName(String fullType) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else {
      return fullType.substring(p + 1);
    }
  }
}
