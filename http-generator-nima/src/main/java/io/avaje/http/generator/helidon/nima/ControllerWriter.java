package io.avaje.http.generator.helidon.nima;

import static io.avaje.http.generator.core.ProcessingContext.diAnnotation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.JsonBUtil;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.PrimitiveUtil;
import io.avaje.http.generator.core.UType;

/**
 * Write Helidon specific web route adapter (a Helidon Service).
 */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-helidon-nima-generator\")";
  private final boolean useJsonB;
  private final Map<String, UType> jsonTypes;

  ControllerWriter(ControllerReader reader, boolean jsonB) throws IOException {
    super(reader);
    this.useJsonB = jsonB;
    if (useJsonB) {
      reader.addImportType("io.avaje.jsonb.Jsonb");
      reader.addImportType("io.avaje.jsonb.JsonType");
      reader.addImportType("io.avaje.jsonb.Types");
      reader.addImportType("io.avaje.jsonb.stream.JsonOutput");
      this.jsonTypes = JsonBUtil.jsonTypes(reader);
      jsonTypes.values().stream()
          .map(UType::importTypes)
          .forEach(reader::addImportTypes);
    } else {
      this.jsonTypes = Map.of();
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

  private List<ControllerMethodWriter> writerMethods() {
    return reader.methods().stream()
      .filter(MethodReader::isWebMethod)
      .map(it -> new ControllerMethodWriter(it, writer, useJsonB))
      .toList();
  }

  private void writeAddRoutes() {
    final var methods = writerMethods();
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
    writer.append(diAnnotation()).eol();
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

    if (instrumentContext) {
      writer.append("  private final RequestContextResolver resolver;").eol();
    }

    for (final UType type : jsonTypes.values()) {
      final var typeString = PrimitiveUtil.wrap(type.shortType()).replace(",", ", ");
      writer.append("  private final JsonType<%s> %sJsonType;", typeString, type.shortName()).eol();
    }
    writer.eol();

    writer.append("  public %s$Route(%s %s", shortName, controllerType, controllerName);
    if (reader.isIncludeValidator()) {
      writer.append(", Validator validator");
    }
    if (useJsonB) {
      writer.append(", Jsonb jsonB");
    }
    if (instrumentContext) {
      writer.append(", RequestContextResolver resolver");
    }

    writer.append(") {").eol();
    writer.append("    this.%s = %s;", controllerName, controllerName).eol();
    if (reader.isIncludeValidator()) {
      writer.append("    this.validator = validator;").eol();
    }
    if (instrumentContext) {
      writer.append("    this.resolver = resolver;").eol();
    }

    if (useJsonB) {
      for (final UType type : jsonTypes.values()) {
        JsonBUtil.writeJsonbType(type, writer);
      }
    }
    writer.append("  }").eol().eol();
  }
}
