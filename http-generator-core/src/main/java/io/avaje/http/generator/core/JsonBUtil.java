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

  public static void writeJsonbType(UType type, Append writer) {

    writer.append("    this.%sJsonType = jsonB.type(", type.shortName());
    if (!type.isGeneric()) {
      writer.append("%s.class)", type.full());
    } else {
      switch (type.mainType()) {
        case "java.util.List":
          writer.append("%s.class).list()", type.param0());
          break;
        case "java.util.Set":
          writer.append("%s.class).set()", type.param0());
          break;
        case "java.util.Map":
          writer.append("%s.class).map()", type.param1());
          break;
        default:
          throw new UnsupportedOperationException(
              "Only java.util Map, Set and List are supported JsonB Controller Collection Types");
      }
    }
    writer.append(";").eol();
  }
}
