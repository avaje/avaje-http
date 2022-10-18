package io.avaje.http.generator.javalin;

import java.io.IOException;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ProcessingContext;

/** Write Javalin specific Controller WebRoute handling adapter. */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-javalin-generator\")";
  private static final String API_BUILDER = "io.javalin.apibuilder.ApiBuilder";
  private final boolean useJsonb;

  ControllerWriter(ControllerReader reader, ProcessingContext ctx, boolean useJsonb)
      throws IOException {

    super(reader, ctx);
    reader.addImportType(API_BUILDER);
    this.useJsonb = useJsonb;
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
    for (final MethodReader method : reader.getMethods()) {
      if (method.isWebMethod()) {
        writeForMethod(method);
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeForMethod(MethodReader method) {
    new ControllerMethodWriter(method, writer, ctx, useJsonb).write(isRequestScoped());
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
    writer.eol();

    writer.append("  public %s$Route(%s %s", shortName, controllerType, controllerName);
    if (reader.isIncludeValidator()) {
      writer.append(", Validator validator");
    }
    writer.append(") {").eol();
    writer.append("    this.%s = %s;", controllerName, controllerName).eol();
    if (reader.isIncludeValidator()) {
      writer.append("    this.validator = validator;").eol();
    }
    writer.append("  }").eol().eol();
  }
}
