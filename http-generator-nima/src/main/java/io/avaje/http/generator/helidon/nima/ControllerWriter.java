package io.avaje.http.generator.helidon.nima;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.lang.model.type.DeclaredType;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ProcessingContext;

/** Write Helidon specific web route adapter (a Helidon Service). */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-helidon-nima-generator\")";
  private final boolean useJsonB;

  ControllerWriter(ControllerReader reader, ProcessingContext ctx, boolean jsonB)
      throws IOException {
    super(reader, ctx);
    useJsonB = jsonB;
    if (useJsonB) {
      reader.addImportType("io.avaje.jsonb.Jsonb");
      reader.addImportType("io.avaje.jsonb.JsonType");
    }
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
        .map(it -> new ControllerMethodWriter(it, writer, ctx, useJsonB))
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
    writer
        .append("public class ")
        .append(shortName)
        .append("$Route implements HttpService {")
        .eol()
        .eol();

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

    List<MethodReader> jsonMethods;
    if (useJsonB) {
      jsonMethods =
          reader.getMethods().stream()
              .filter(MethodReader::isWebMethod)
              .filter(m -> !"byte[]".equals(m.getReturnType().toString()))
              .filter(
                  m -> m.getProduces() == null || m.getProduces().toLowerCase().contains("json"))
              .toList();
      writeJsonBTypeFields(jsonMethods);
    } else {
      jsonMethods = null;
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
      writeJsonBTypeAssignments(jsonMethods);
    }

    writer.append("  }").eol().eol();
  }

  public void writeJsonBTypeFields(List<MethodReader> jsonMethods) {
    for (final MethodReader methodReader : jsonMethods) {
      // body types
      if (methodReader.getBodyType() != null) {
        methodReader.getParams().stream()
            .filter(MethodParam::isBody)
            .forEach(
                param ->
                    writer
                        .append(
                            "  private final JsonType<%s> %sBodyJsonType;",
                            param.getUType().full(), methodReader.simpleName())
                        .eol());
      }

      if (methodReader.isVoid()) {
        continue;
      }

      // return types
      if (methodReader.getReturnType() instanceof final DeclaredType fullType) {
        final var typeArgs = fullType.getTypeArguments();
        final var typeArgSize = typeArgs.size();

        writer.append("  private final JsonType<");
        switch (typeArgSize) {
          case 1 -> {
            if (fullType.toString().contains("java.util.Set"))
              writer.append("java.util.Set<%s>>", typeArgs.get(0));
            else writer.append("java.util.List<%s>>", typeArgs.get(0));
          }
          case 2 -> writer.append("java.util.Map<String, %s>>", typeArgs.get(1));
          default -> writer.append("%s>", fullType);
        }
        writer.append(" %sReturnedJsonType;", methodReader.simpleName()).eol();
      } else {
        throw new UnsupportedOperationException(
            "Only Objects are supported with Jsonb Return Types");
      }
    }
  }

  public void writeJsonBTypeAssignments(List<MethodReader> jsonMethods) {

    for (final MethodReader methodReader : jsonMethods) {
      // body types
      writeBodyJsonType(methodReader);

      if (methodReader.isVoid()) {
        continue;
      }

      writeReturnJsonType(methodReader);
    }
  }

  private void writeBodyJsonType(MethodReader methodReader) {

    if (methodReader.getBodyType() != null) {
      methodReader.getParams().stream()
          .filter(MethodParam::isBody)
          .forEach(
              p -> {
                final var type = p.getUType();
                final var jsonType =
                    Optional.ofNullable(type.param1())
                        .or(() -> Optional.ofNullable(type.param0()))
                        .orElseGet(type::full);
                writer.append(
                    "    this.%sBodyJsonType = jsonB.type(%s.class)",
                    methodReader.simpleName(), jsonType);

                if (type.param0() != null) {
                  writer.append(".");
                  switch (type.mainType()) {
                    case "java.util.List" -> writer.append("list");
                    case "java.util.Map" -> writer.append("map");
                    case "java.util.Set" -> writer.append("set");
                    default -> throw new UnsupportedOperationException(
                        "Only java.util Map, Set and List are supported JsonB Controller Body Return Types");
                  }
                  writer.append("()");
                }
                writer.append(";").eol();
              });
    }
  }

  void writeReturnJsonType(MethodReader methodReader) {

    if (methodReader.getReturnType() instanceof final DeclaredType fullType) {
      final var typeArgs = fullType.getTypeArguments();
      final var typeArgSize = typeArgs.size();
      final var jsonType =
          switch (typeArgSize) {
            case 1 -> typeArgs.get(0);
            case 2 -> typeArgs.get(1);
            default -> fullType;
          };

      writer.append(
          "    this.%sReturnedJsonType = jsonB.type(%s.class)",
          methodReader.simpleName(), jsonType);
      final var returnType = fullType.toString();
      if (typeArgSize != 0) {
        writer.append(".");

        switch (returnType.substring(0, 13)) {
          case "java.util.Lis" -> writer.append("list");
          case "java.util.Map" -> writer.append("map");
          case "java.util.Set" -> writer.append("set");
          default -> throw new UnsupportedOperationException(
              "Only java.util Map, Set and List are supported JsonB Controller Collection Return Types");
        }

        writer.append("()");
      }
      writer.append(";").eol();

    } else {
      throw new UnsupportedOperationException(
          "Only Objects and Strings are supported with Jsonb Controller Return Types");
    }
  }
}
