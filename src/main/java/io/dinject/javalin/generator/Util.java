package io.dinject.javalin.generator;

import java.util.LinkedHashSet;
import java.util.Set;

class Util {

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

  static String shortName(String fullType) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else {
      return fullType.substring(p + 1);
    }
  }

  static String snakeCase(String name) {

    StringBuilder sb = new StringBuilder(name.length() + 5);

    int len = name.length();
    for (int i = 0; i < len; i++) {
      char ch = name.charAt(i);
      if (Character.isUpperCase(ch)) {
        if (i > 0) {
          sb.append("-");
        }
        sb.append(Character.toLowerCase(ch));
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }
}
