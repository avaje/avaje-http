package io.avaje.http.generator.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JsonBUtil {
  private JsonBUtil() {}

  /**
   * Return true if avaje-jsonb is detected in the classpath.
   */
  public static boolean detectJsonb() {
    try {
      Class.forName("io.avaje.jsonb.Jsonb");
      return true;
    } catch (final ClassNotFoundException e) {
      return false;
    }
  }

  public static Map<String, UType> jsonTypes(ControllerReader reader) {

    final Map<String, UType> jsonTypes = new LinkedHashMap<>();
    final Consumer<UType> addToMap = uType -> jsonTypes.put(uType.full(), uType);

    reader.methods().stream()
        .filter(MethodReader::isWebMethod)
        .filter(m -> !"byte[]".equals(m.returnType().toString()))
        .filter(m -> m.produces() == null || m.produces().toLowerCase().contains("json"))
        .forEach(
            methodReader -> {
              addJsonBodyType(methodReader, addToMap);
              if (!methodReader.isVoid()) {
                var uType = UType.parse(methodReader.returnType());

                if ("java.util.concurrent.CompletableFuture".equals(uType.mainType())) {
                  uType = uType.paramRaw();
                }

                addToMap.accept(uType);
              }
            });

    return Map.copyOf(jsonTypes);
  }

  private static void addJsonBodyType(MethodReader methodReader, Consumer<UType> addToMap) {
    if (methodReader.bodyType() != null) {
      methodReader.params().stream()
          .filter(MethodParam::isBody)
          .map(MethodParam::utype)
          .filter(s -> !s.full().startsWith("java.io.InputStream"))
          .filter(s -> !s.full().startsWith("byte[]"))
          .forEach(addToMap);
    }
  }

  public static void writeJsonbType(UType type, Append writer) {
    writer.append("    this.%sJsonType = jsonB.type(", type.shortName());
    if (!type.isGeneric()) {
      writer.append("%s.class)", Util.shortName(PrimitiveUtil.wrap(type.full())));
    } else {
      switch (type.mainType()) {
        case "java.util.List":
          writeType(type.paramRaw(), writer);
          writer.append(".list()");
          break;
        case "java.util.Set":
          writeType(type.paramRaw(), writer);
          writer.append(".set()");
          break;
        case "java.util.Map":
          writeType(type.paramRaw(), writer);
          writer.append(".map()");
          break;
        default: {
          if (type.mainType().contains("java.util")) {
            throw new UnsupportedOperationException("Only java.util Map, Set and List are supported JsonB Controller Collection Types");
          }
          writeType(type, writer);
        }
      }
    }
    writer.append(";").eol();
  }

  static void writeType(UType type, Append writer) {
    if (type.isGeneric()) {
      final var params =
          type.importTypes().stream()
              .skip(1)
              .map(Util::shortName)
              .collect(Collectors.joining(".class, "));

      writer.append("Types.newParameterizedType(%s.class, %s.class))", Util.shortName(type.mainType()), params);
    } else {
      writer.append("%s.class)", Util.shortName(type.mainType()));
    }
  }
}
