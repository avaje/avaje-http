package io.avaje.http.generator.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonBUtil {
  private JsonBUtil() {}

  private static final Map<String, UType> jsonTypes = new LinkedHashMap<>();

  public static Map<String, UType> getJsonTypes(ControllerReader reader) {
    reader.getMethods().stream()
        .filter(MethodReader::isWebMethod)
        .filter(m -> !"byte[]".equals(m.getReturnType().toString()))
        .filter(m -> m.getProduces() == null || m.getProduces().toLowerCase().contains("json"))
        .forEach(
            methodReader -> {
              addJsonBodyType(methodReader);
              if (!methodReader.isVoid()) {
                addJsonType(UType.parse(methodReader.getReturnType()));
              }
            });

    return Map.copyOf(jsonTypes);
  }

  private static void addJsonType(UType type) {
    jsonTypes.put(type.full(), type);
  }

  private static void addJsonBodyType(MethodReader methodReader) {
    if (methodReader.getBodyType() != null) {
      methodReader.getParams().stream()
          .filter(MethodParam::isBody)
          .forEach(param -> addJsonType(param.getUType()));
    }
  }
}
