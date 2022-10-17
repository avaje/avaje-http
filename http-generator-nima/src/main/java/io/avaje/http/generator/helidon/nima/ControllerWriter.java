package io.avaje.http.generator.helidon.nima;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.http.generator.core.UType;

/** Write Helidon specific web route adapter (a Helidon Service). */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-helidon-nima-generator\")";
  private final boolean useJsonB;
  private final Map<String, JsonbType> jsonTypes;

  ControllerWriter(ControllerReader reader, ProcessingContext ctx, boolean jsonB)
      throws IOException {
    super(reader, ctx);
    useJsonB = jsonB;
    if (useJsonB) {
      reader.addImportType("io.avaje.jsonb.Jsonb");
      reader.addImportType("io.avaje.jsonb.JsonType");
      jsonTypes = new HashMap<>();
    } else {
      jsonTypes = null;
    }
    reader.addImportType("io.helidon.common.http.HttpMediaType");
    reader.addImportType("io.helidon.common.parameters.Parameters");
    reader.addImportType("io.helidon.nima.webserver.http.HttpRules");
    reader.addImportType("io.helidon.nima.webserver.http.HttpRouting");
    reader.addImportType("io.helidon.nima.webserver.http.ServerRequest");
    reader.addImportType("io.helidon.nima.webserver.http.ServerResponse");
    reader.addImportType("io.helidon.nima.webserver.http.HttpService");
  }

  void write() {
    writePackage();
    writeImports();
    writeClassStart();
    writeAddRoutes();
    writeClassEnd();
  }

  private List<ControllerMethodWriter> getWriterMethods() {
    return reader.getMethods().stream()
        .filter(MethodReader::isWebMethod)
        .map(it -> new ControllerMethodWriter(it, writer, ctx, useJsonB, jsonTypes))
        .toList();
  }

  private void writeAddRoutes() {
    final var methods = getWriterMethods();
    writeRoutes(methods);
    for (final ControllerMethodWriter methodWriter : methods) {
      methodWriter.writeHandler(isRequestScoped());
    }
  }

  private void writeRoutes(List<ControllerMethodWriter> methods) {
    writer.append("  @Override").eol();
    writer.append("  public void routing(HttpRules rules) {").eol();

    for (final ControllerMethodWriter methodWriter : methods) {
      methodWriter.writeRule();
      if (!reader.isDocHidden()) {
        methodWriter.buildApiDocumentation();
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    writer.append("@Component").eol();
    writer.append("public class %s$Route implements HttpService {", shortName).eol().eol();

    var controllerName = "controller";
    var controllerType = shortName;
    if (isRequestScoped()) {
      controllerName = "factory";
      controllerType += Constants.FACTORY_SUFFIX;
    }
    writer.append("  private final %s %s;", controllerType, controllerName).eol();

    if (reader.isIncludeValidator()) {
      writer.append("  private final Validator validator;").eol();
    }

    if (useJsonB) {

      final var jsonMethods =
          reader.getMethods().stream()
              .filter(MethodReader::isWebMethod)
              .filter(m -> !"byte[]".equals(m.getReturnType().toString()))
              .filter(
                  m -> m.getProduces() == null || m.getProduces().toLowerCase().contains("json"))
              .toList();
      writeJsonBTypeFields(jsonMethods);
    }
    writer.eol();

    writer.append("  public %s$Route(%s %s", shortName, controllerType, controllerName);
    if (reader.isIncludeValidator()) {
      writer.append(", Validator validator");
    }
    if (useJsonB) {
      writer.append(", Jsonb jsonB");
    }
    writer.append(") {").eol();
    writer.append("    this.%s = %s;", controllerName, controllerName).eol();
    if (reader.isIncludeValidator()) {
      writer.append("    this.validator = validator;").eol();
    }
    if (useJsonB) {
      writeJsonBTypeAssignments();
    }
    writer.append("  }").eol().eol();
  }

  public void writeJsonBTypeFields(List<MethodReader> jsonMethods) {
    for (final MethodReader methodReader : jsonMethods) {

      writeFieldJsonBodyType(methodReader);
      if (methodReader.isVoid()) {
        continue;
      }
      writeFieldJsonReturnType(methodReader);
    }
  }

  public void writeFieldJsonBodyType(MethodReader methodReader) {
    // body types
    if (methodReader.getBodyType() != null) {
      methodReader.getParams().stream()
          .filter(MethodParam::isBody)
          .forEach(
              param -> {
                final var fullType = param.getUType().full();
                jsonTypes.computeIfAbsent(
                    fullType,
                    type -> {
                      final var baseType = getBaseType(param.getUType());
                      final var fieldName = createFieldName(baseType, type);
                      writer.append("private final JsonType<%s> %sJsonType;", type, fieldName).eol();
                      return new JsonbType(baseType, fieldName);
                    });
              });
    }
  }

  public void writeFieldJsonReturnType(MethodReader methodReader) {

    // return types
    if (methodReader.getReturnType() instanceof final DeclaredType fullType) {
      final var fullTypeString = fullType.toString();
      jsonTypes.computeIfAbsent(
          fullTypeString,
          type -> {
            final var baseType = getBaseType(fullType);
            final var fieldName = createFieldName(baseType, type);
            writer.append("private final JsonType<%s> %sJsonType;", type, fieldName).eol();
            return new JsonbType(baseType, fieldName);
          });
    } else if (methodReader.getReturnType() instanceof final PrimitiveType fullType) {

      jsonTypes.computeIfAbsent(
          fullType.toString(),
          type -> {
            final var baseType =
                "int".equals(type) ? "Integer" : type.substring(0, 1).toUpperCase() + type.substring(1);
            writer.append("private final JsonType<%s> %sJsonType;", baseType, type).eol();
            return new JsonbType(baseType, type);
          });
    } else {
      throw new UnsupportedOperationException(
          "Only Primitives and Objects are supported with Jsonb Return Types");
    }
  }

  public void writeJsonBTypeAssignments() {
    for (final var entry : jsonTypes.entrySet()) {
      final var fullType = entry.getKey();
      final var element = entry.getValue();
      final var fieldName = element.fieldName();
      final var baseType = element.baseType();

      writer.append("    this.%sJsonType = jsonB.type(%s.class)", fieldName, baseType);

      if (fullType.contains("<")) {
        writer.append(".");
        switch (fullType.substring(0, 13)) {
          case "java.util.Lis" -> writer.append("list");
          case "java.util.Map" -> writer.append("map");
          case "java.util.Set" -> writer.append("set");
          default -> throw new UnsupportedOperationException(
              "Only java.util Map, Set and List are supported JsonB Controller Collection Types");
        }
        writer.append("()");
      }
      writer.append(";").eol();
    }
  }

  public static String getBaseType(UType type) {

    return Optional.ofNullable(type.param1())
        .or(() -> Optional.ofNullable(type.param0()))
        .orElseGet(type::full);
  }

  private static String getBaseType(DeclaredType type) {
    final var typeArgs = type.getTypeArguments();
    return switch (typeArgs.size()) {
      case 1 -> typeArgs.get(0).toString();

      case 2 -> typeArgs.get(1).toString();
      default -> type.toString();
    };
  }

  private static String createFieldName(String baseType, String fullType) {
    final var shortType =
        baseType
            .substring(baseType.lastIndexOf('.') + 1)
            .transform(str -> str.substring(0, 1).toLowerCase() + str.substring(1));

    if (fullType.length() <= 13) return shortType;

    return switch (fullType.substring(0, 13)) {
      case "java.util.Lis" -> "List";
      case "java.util.Map" -> "Map";
      case "java.util.Set" -> "Set";
      default -> shortType;
    };
  }
}
