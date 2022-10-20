package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.*;
import java.io.IOException;
import java.util.Map;

/** Write Javalin specific Controller WebRoute handling adapter. */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-javalin-generator\")";
  private static final String API_BUILDER = "io.javalin.apibuilder.ApiBuilder";
  private final boolean useJsonB;
  private final Map<String, UType> jsonTypes;

  ControllerWriter(ControllerReader reader, ProcessingContext ctx, boolean jsonB)
      throws IOException {
    super(reader, ctx);
    this.useJsonB = jsonB;
    if (useJsonB) {
      reader.addImportType("io.avaje.jsonb.Jsonb");
      reader.addImportType("io.avaje.jsonb.JsonType");
      this.jsonTypes = JsonBUtil.jsonTypes(reader);
    } else {
      this.jsonTypes = Map.of();
    }
    reader.addImportType(API_BUILDER);
  }

  void write() {
    writePackage();
    writeImports();
    writeClassStart();
    writeAddRoutes();
    writeClassEnd();
  }

  private void writeAddRoutes() {
    writer.append("  @Override").eol();
    writer.append("  public void registerRoutes() {").eol().eol();
    for (final MethodReader method : reader.methods()) {
      if (method.isWebMethod()) {
        writeForMethod(method);
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeForMethod(MethodReader method) {
    new ControllerMethodWriter(method, writer, ctx, useJsonB).write(isRequestScoped());
    if (!reader.isDocHidden()) {
      method.buildApiDocumentation(ctx);
    }
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    writer.append("@Component").eol();
    writer
        .append("public class ")
        .append(shortName)
        .append("$Route implements WebRoutes {")
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

    for (final UType type : jsonTypes.values()) {
      writer
          .append(
              "  private final JsonType<%s> %sJsonType;",
              PrimitiveUtil.wrap(type.full()), type.shortName())
          .eol();
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
      for (final UType type : jsonTypes.values()) {
        JsonBUtil.writeJsonbType(type, writer);
      }
    }
    writer.append("  }").eol().eol();
  }
}
