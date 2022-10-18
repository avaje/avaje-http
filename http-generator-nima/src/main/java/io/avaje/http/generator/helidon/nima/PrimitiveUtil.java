package io.avaje.http.generator.helidon.nima;

import java.util.HashMap;
import java.util.Map;

final class PrimitiveUtil {

  static Map<String,String> wrapperMap = new HashMap<>();
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

  static String wrap(String shortName) {
    String wrapped = wrapperMap.get(shortName);
    return wrapped != null ? wrapped : shortName;
  }
}
