package io.avaje.http.generator.javalin;

import static io.avaje.http.generator.core.ProcessingContext.diAnnotation;

import java.io.IOException;
import java.util.Map;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.JsonBUtil;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.PrimitiveUtil;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.http.generator.core.UType;

/**
 * Write Javalin specific Controller WebRoute handling adapter.
 */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-javalin-generator\")";
  private final boolean useJsonB;
  private final Map<String, UType> jsonTypes;
  private final boolean javalin6 = ProcessingContext.javalin6();

  ControllerWriter(ControllerReader reader, boolean jsonb) throws IOException {
    super(reader);
    this.useJsonB = jsonb;

    if (useJsonB) {
      reader.addImportType("io.avaje.jsonb.Jsonb");
      reader.addImportType("io.avaje.jsonb.JsonType");
      reader.addImportType("io.avaje.jsonb.Types");
      this.jsonTypes = JsonBUtil.jsonTypes(reader);
      jsonTypes.values().stream().map(UType::importTypes).forEach(reader::addImportTypes);
    } else {
      this.jsonTypes = Map.of();
    }
    reader.addImportType("io.javalin.plugin.Plugin");

    if (javalin6) {

      reader.addImportType("io.javalin.config.JavalinConfig");
      reader.addImportType("io.javalin.router.JavalinDefaultRouting");
      reader.addImportType("io.avaje.http.api.AvajeJavalinPlugin");
    } else {
      reader.addImportType("io.javalin.Javalin");
    }
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

    if (javalin6) {
      writer.append("  public void onStart(JavalinConfig cfg) {").eol();
      writer.append("    cfg.router.mount(this::routes);").eol();
      writer.append("  }").eol().eol();

      writer.append("  private void routes(JavalinDefaultRouting app) {").eol().eol();
    } else {
      writer.append("  public void apply(Javalin app) {").eol().eol();
    }

    for (final MethodReader method : reader.methods()) {
      if (method.isWebMethod()) {
        writeForMethod(method);
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeForMethod(MethodReader method) {
    new ControllerMethodWriter(method, writer, useJsonB).write(isRequestScoped());
    if (!reader.isDocHidden()) {
      method.buildApiDocumentation();
    }
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    writer.append(diAnnotation()).eol();
    writer
        .append("public class ")
        .append(shortName)
        .append(javalin6 ? "$Route extends AvajeJavalinPlugin {" : "$Route implements Plugin {")
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
      writer.append(", Jsonb jsonb");
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
