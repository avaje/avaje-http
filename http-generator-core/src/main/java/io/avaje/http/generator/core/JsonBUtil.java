package io.avaje.http.generator.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class JsonBUtil {

  private static final JsonBDetect NO_JSONB = new JsonBDetect();
  /**
   * Detect JsonB use and handle imports as needed.
   */
  public static final class JsonBDetect {
    private final Map<String, UType> jsonTypes;

    private JsonBDetect() {
      this.jsonTypes = Map.of();
    }

    private JsonBDetect(ControllerReader reader) {
      this.jsonTypes = JsonBUtil.jsonTypes(reader);
      addImports(reader);
    }

    private void addImports(ControllerReader reader) {
      reader.addImportType("io.avaje.jsonb.Jsonb");
      reader.addImportType("io.avaje.jsonb.JsonType");
      reader.addImportType("io.avaje.jsonb.Types");
      jsonTypes.values().stream().map(UType::importTypes).forEach(reader::addImportTypes);
    }

    public boolean useJsonB() {
      return !jsonTypes.isEmpty();
    }

    public Map<String, UType> jsonTypes() {
      return jsonTypes;
    }
  }

  private JsonBUtil() {}

  public static boolean isJsonMimeType(String producesMimeType) {
    return producesMimeType == null || producesMimeType.toLowerCase().contains("application/json");
  }

  public static JsonBDetect detect(boolean jsonb, ControllerReader reader) {
    return !jsonb ? NO_JSONB : new JsonBDetect(reader);
  }

  public static Map<String, UType> jsonTypes(ControllerReader reader) {
    final Map<String, UType> jsonTypes = new LinkedHashMap<>();
    final Consumer<UType> addToMap = uType -> jsonTypes.put(uType.full(), uType);

    reader.methods().stream()
        .filter(MethodReader::isWebMethod)
        .filter(m -> m.webMethod() instanceof CoreWebMethod)
        .filter(m -> m.webMethod() != CoreWebMethod.FILTER)
        .filter(m -> !"byte[]".equals(m.returnType().toString()))
        .filter(m -> m.produces() == null || m.produces().toLowerCase().contains("json"))
        .forEach(
            methodReader -> {
              if (!methodReader.isErrorMethod()) {
                addJsonBodyType(methodReader, addToMap);
              }
              final var asTypeElement = APContext.asTypeElement(methodReader.returnType());
              if (!methodReader.isVoid() && (asTypeElement == null || !JStachePrism.isPresent(asTypeElement))) {
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
    writer.append("    this.%sJsonType = jsonb.type(", type.shortName());
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
          type.params().stream()
              .map(Util::shortName)
              .map(s -> "?".equals(s) ? "Object" : s)
              .collect(Collectors.joining(".class, "));

      writer.append(
          "Types.newParameterizedType(%s.class, %s.class))",
          Util.shortName(type.mainType()), params);
    } else {
      writer.append("%s.class)", Util.shortName(type.mainType()));
    }
  }
}
