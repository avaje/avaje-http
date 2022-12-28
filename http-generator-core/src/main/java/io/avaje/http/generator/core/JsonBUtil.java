package io.avaje.http.generator.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JsonBUtil {
  private JsonBUtil() {}

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
                addToMap.accept(UType.parse(methodReader.returnType()));
              }
            });

    return Map.copyOf(jsonTypes);
  }

  private static void addJsonBodyType(MethodReader methodReader, Consumer<UType> addToMap) {
    if (methodReader.bodyType() != null) {
      methodReader.params().stream()
          .filter(MethodParam::isBody)
          .map(MethodParam::utype)
          .forEach(addToMap);
    }
  }

  public static void writeJsonbType(UType type, Append writer) {

    writer.append("    this.%sJsonType = jsonB.type(", type.shortName());
    if (!type.isGeneric()) {
      writer.append("%s.class)", PrimitiveUtil.wrap(type.full()));
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
          {
            try {
              if (Collection.class.isAssignableFrom(Class.forName(type.mainType())))
                throw new UnsupportedOperationException(
                    "Only java.util Map, Set and List are supported JsonB Controller Collection Types");
            } catch (final ClassNotFoundException e) {
              throw new UnsupportedOperationException(
                  "Only java.util Map, Set and List are supported JsonB Controller Collection Types");
            }
            final var params =
                type.allTypes().stream().skip(1).collect(Collectors.joining(".class, ")) + ".class";

            writer.append("Types.newParameterizedType(%s.class, %s))", type.mainType(), params);
          }
      }
    }
    writer.append(";").eol();
  }
}
