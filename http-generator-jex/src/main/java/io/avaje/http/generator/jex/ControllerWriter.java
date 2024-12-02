package io.avaje.http.generator.jex;

import static io.avaje.http.generator.core.ProcessingContext.diAnnotation;
import io.avaje.http.generator.core.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Write Jex specific Controller WebRoute handling adapter.
 */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-jex-generator\")";
  private static final String API_CONTEXT = "io.avaje.jex.Context";
  private static final String API_ROUTING = "io.avaje.jex.Routing";
  private final boolean useJsonB;
  private final Map<String, UType> jsonTypes;

  ControllerWriter(ControllerReader reader, boolean jsonb) throws IOException {
    super(reader);
    this.useJsonB = jsonb;
    reader.addImportType(API_CONTEXT);
    reader.addImportType(API_ROUTING);
    reader.addImportType("java.io.IOException");
    if (reader.methods().stream()
        .map(MethodReader::webMethod)
        .anyMatch(w -> CoreWebMethod.FILTER == w)) {
      reader.addImportType("io.avaje.jex.HttpFilter.FilterChain");
    }
    if (reader.methods().stream()
      .map(MethodReader::hxRequest)
      .anyMatch(Objects::nonNull)) {
      reader.addImportType("io.avaje.jex.htmx.HxHandler");
    }
    if (reader.html()) {
      reader.addImportType("io.avaje.jex.htmx.TemplateRender");
      if (reader.hasContentCache()) {
        reader.addImportType("io.avaje.jex.htmx.TemplateContentCache");
      }
    }
    if (useJsonB) {
      reader.addImportType("io.avaje.jsonb.Jsonb");
      reader.addImportType("io.avaje.jsonb.JsonType");
      reader.addImportType("io.avaje.jsonb.Types");
      this.jsonTypes = JsonBUtil.jsonTypes(reader);
      jsonTypes.values().stream().map(UType::importTypes).forEach(reader::addImportTypes);
    } else {
      this.jsonTypes = Map.of();
    }
  }

  void write() {
    writePackage();
    writeImports();
    writeClassStart();
    writeAddRoutes();
    writeHandlers();
    writeClassEnd();
  }

  private void writeAddRoutes() {
    writer.append("  @Override").eol();
    writer.append("  public void add(Routing routing) {").eol();
    for (MethodReader method : reader.methods()) {
      if (method.isWebMethod()) {
        writeRouting(method);
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeHandlers() {
    for (MethodReader method : reader.methods()) {
      if (method.isWebMethod()) {
        new ControllerMethodWriter(method, writer, reader, useJsonB).writeHandler(isRequestScoped());
        if (!reader.isDocHidden()) {
          method.buildApiDocumentation();
        }
      }
    }
  }

  private void writeRouting(MethodReader method) {
    new ControllerMethodWriter(method, writer, reader, useJsonB).writeRouting();
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    writer.append(diAnnotation()).eol();
    writer.append("public final class ").append(shortName).append("$Route implements Routing.HttpService {").eol().eol();

    String controllerName = "controller";
    String controllerType = shortName;
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

    if (reader.html()) {
      writer.append("  private final TemplateRender renderer;").eol();
      if (reader.hasContentCache()) {
        writer.append("  private final TemplateContentCache contentCache;").eol();
      }
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
      writer.append(", Jsonb jsonb");
    }
    if (instrumentContext) {
      writer.append(", RequestContextResolver resolver");
    }
    if (reader.html()) {
      writer.append(", TemplateRender renderer");
      if (reader.hasContentCache()) {
        writer.append(", TemplateContentCache contentCache");
      }
    }
    writer.append(") {").eol();
    writer.append("    this.%s = %s;", controllerName, controllerName).eol();
    if (reader.isIncludeValidator()) {
      writer.append("    this.validator = validator;").eol();
    }
    if (instrumentContext) {
      writer.append("    this.resolver = resolver;").eol();
    }
    if (reader.html()) {
      writer.append("    this.renderer = renderer;").eol();
      if (reader.hasContentCache()) {
        writer.append("    this.contentCache = contentCache;").eol();
      }
    }
    if (useJsonB) {
      for (final UType type : jsonTypes.values()) {
        JsonBUtil.writeJsonbType(type, writer);
      }
    }
    writer.append("  }").eol().eol();
  }

}
