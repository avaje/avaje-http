package io.avaje.http.generator.helidon.nima;

import static io.avaje.http.generator.core.ProcessingContext.diAnnotation;
import static io.avaje.http.generator.core.ProcessingContext.isAssignable2Interface;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.avaje.http.generator.core.*;

/**
 * Write Helidon specific web route adapter (a Helidon Service).
 */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-helidon-generator\")";
  private static final String IMPORT_HTTP_STATUS = "import static io.helidon.http.Status.*;";

  private static final String JSON_JsonOutput = "io.avaje.json.stream.JsonOutput";
  private static final String JSONB_JsonOutput = "io.avaje.jsonb.stream.JsonOutput";

  private final boolean useJsonB;
  private final Map<String, UType> jsonTypes;

  ControllerWriter(ControllerReader reader, boolean jsonb) throws IOException {
    super(reader);
    this.useJsonB = jsonb;
    if (useJsonB) {
      reader.addImportType("io.avaje.jsonb.Jsonb");
      reader.addImportType("io.avaje.jsonb.JsonType");
      reader.addImportType("io.avaje.jsonb.Types");
      reader.addImportType(jsonOutputType());
      this.jsonTypes = JsonBUtil.jsonTypes(reader);
      jsonTypes.values().stream()
          .map(UType::importTypes)
          .forEach(reader::addImportTypes);
    } else {
      this.jsonTypes = Map.of();
    }
    reader.addImportType("io.helidon.common.media.type.MediaTypes");
    reader.addImportType("io.helidon.common.parameters.Parameters");
    reader.addImportType("io.helidon.webserver.http.HttpRouting");
    reader.addImportType("io.helidon.webserver.http.ServerRequest");
    reader.addImportType("io.helidon.webserver.http.ServerResponse");
    reader.addImportType("io.helidon.webserver.http.HttpFeature");
    reader.addImportType("io.helidon.http.HeaderNames");
    if (!reader.roles().isEmpty() || reader.methods().stream().anyMatch(m -> !m.roles().isEmpty())) {
      reader.addImportType("io.helidon.webserver.security.SecurityFeature");
    }
    if (reader.isIncludeValidator()) {
      reader.addImportType("io.helidon.http.HeaderName");
    }
    if (reader.methods().stream()
        .map(MethodReader::webMethod)
        .anyMatch(w -> CoreWebMethod.FILTER == w)) {
      reader.addImportType("io.helidon.webserver.http.FilterChain");
      reader.addImportType("io.helidon.webserver.http.RoutingRequest");
      reader.addImportType("io.helidon.webserver.http.RoutingResponse");
    }
    if (reader.methods().stream()
      .map(MethodReader::hxRequest)
      .anyMatch(Objects::nonNull)) {
      reader.addImportType("io.avaje.htmx.nima.HxHandler");
    }
    if (reader.html()) {
      reader.addImportType("io.avaje.htmx.nima.TemplateRender");
      if (reader.hasContentCache()) {
        reader.addImportType("io.avaje.htmx.nima.TemplateContentCache");
      }
    }
  }

  private static String jsonOutputType() {
    return ProcessingContext.typeElement(JSON_JsonOutput) != null
      ? JSON_JsonOutput
      : JSONB_JsonOutput;
  }

  void write() {
    writePackage();
    writeImports();
    writeClassStart();
    writeAddRoutes();
    writeClassEnd();
  }

  @Override
  protected void writeImports() {
    if (router) {
      writer.append(IMPORT_HTTP_STATUS).eol();
    }
    super.writeImports();
  }

  private List<ControllerMethodWriter> writerMethods() {
    return reader.methods().stream()
      .filter(MethodReader::isWebMethod)
      .map(it -> new ControllerMethodWriter(it, writer, useJsonB, reader))
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
    writer.append("  public void setup(HttpRouting.Builder routing) {").eol();

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
    writer.append("public final class %s$Route implements HttpFeature {", shortName).eol().eol();

    var controllerName = "controller";
    var controllerType = shortName;
    if (isRequestScoped()) {
      controllerName = "factory";
      controllerType += Constants.FACTORY_SUFFIX;
    }

    if (reader.isIncludeValidator()) {
      writer.append("  private static final HeaderName HEADER_ACCEPT_LANGUAGE = HeaderNames.create(\"Accept-Language\");").eol();
    }
    if (reader.html()) {
      writer.append("  private static final io.helidon.common.media.type.MediaType HTML_UTF8 = MediaTypes.create(\"text/html;charset=UTF8\");").eol();
    }

    writer.append("  private final %s %s;", controllerType, controllerName).eol();

    if (reader.isIncludeValidator()) {
      writer.append("  private final Validator validator;").eol();
    }
    if (instrumentContext) {
      writer.append("  private final RequestContextResolver resolver;").eol();
    }
    if (reader.html()) {
      writer.append("  private final TemplateRender renderer;").eol();
      if (reader.hasContentCache()) {
        writer.append("  private final TemplateContentCache contentCache;").eol();
      }
    }

    for (final UType type : jsonTypes.values()) {
      if (!isInputStream(type.full())) {
        final var typeString = PrimitiveUtil.wrap(type.shortType()).replace(",", ", ");
        writer.append("  private final JsonType<%s> %sJsonType;", typeString, type.shortName()).eol();
      }
    }
    writer.eol();

    writer.append("  public %s$Route(%s %s", shortName, controllerType, controllerName);
    if (reader.isIncludeValidator()) {
      writer.append(", Validator validator");
    }
    if (useJsonB) {
      writer.append(", Jsonb jsonb");
    }
    if (reader.html()) {
      writer.append(", TemplateRender renderer");
      if (reader.hasContentCache()) {
        writer.append(", TemplateContentCache contentCache");
      }
    }
    if (instrumentContext) {
      writer.append(", RequestContextResolver resolver");
    }

    writer.append(") {").eol();
    writer.append("    this.%s = %s;", controllerName, controllerName).eol();
    if (reader.isIncludeValidator()) {
      writer.append("    this.validator = validator;").eol();
    }
    if (reader.html()) {
      writer.append("    this.renderer = renderer;").eol();
      if (reader.hasContentCache()) {
        writer.append("    this.contentCache = contentCache;").eol();
      }
    }
    if (instrumentContext) {
      writer.append("    this.resolver = resolver;").eol();
    }

    if (useJsonB) {
      for (final UType type : jsonTypes.values()) {
        if (!isInputStream(type.full())) {
          JsonBUtil.writeJsonbType(type, writer);
        }
      }
    }
    writer.append("  }").eol().eol();

    if (reader.isIncludeValidator()) {
      writer.append("  private String language(ServerRequest req) {").eol();
      writer.append("    var headers = req.headers();").eol();
      writer.append("    if (headers.contains(HEADER_ACCEPT_LANGUAGE)) {").eol();
      writer.append("      return headers.get(HEADER_ACCEPT_LANGUAGE).get();").eol();
      writer.append("    }").eol();
      writer.append("    return null;").eol();
      writer.append("  }").eol().eol();
    }
  }

  private boolean isInputStream(String type) {
    return isAssignable2Interface(type, "java.io.InputStream");
  }
}
