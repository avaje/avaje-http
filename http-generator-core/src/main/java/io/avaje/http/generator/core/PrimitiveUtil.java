package io.avaje.http.generator.core;

import java.util.HashMap;
import java.util.Map;

public final class PrimitiveUtil {

  private PrimitiveUtil() {}

  static final Map<String, String> wrapperMap = new HashMap<>();

  static {
    wrapperMap.put("char", "Character");
    wrapperMap.put("byte", "Byte");
    wrapperMap.put("int", "Integer");
    wrapperMap.put("long", "Long");
    wrapperMap.put("short", "Short");
    wrapperMap.put("double", "Double");
    wrapperMap.put("float", "Float");
    wrapperMap.put("boolean", "Boolean");
  }

  public static String wrap(String shortName) {
    final var wrapped = wrapperMap.get(shortName);
    return wrapped != null ? wrapped : shortName;
  }
}
