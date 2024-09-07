package io.avaje.http.generator.sigma;

import static io.avaje.http.generator.core.ProcessingContext.diAnnotation;

import java.io.IOException;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.MethodReader;

/**
 * Write Sigma specific Controller WebRoute handling adapter.
 */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-sigma-generator\")";

  ControllerWriter(ControllerReader reader) throws IOException {
    super(reader);
    reader.addImportType("io.avaje.sigma.HttpService");
    reader.addImportType("io.avaje.sigma.Router");
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
    writer.append("  public void setup(Router router) {").eol().eol();


    for (final MethodReader method : reader.methods()) {
      if (method.isWebMethod()) {
        writeForMethod(method);
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeForMethod(MethodReader method) {
    new ControllerMethodWriter(method, writer).write();
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
      .append("$Route implements HttpService {")
      .eol()
      .eol();

    var controllerName = "controller";
    var controllerType = shortName;
    writer.append("  private final %s %s;", controllerType, controllerName).eol();

    if (reader.isIncludeValidator()) {
      writer.append("  private final Validator validator;").eol();
    }

    if (instrumentContext) {
      writer.append("  private final RequestContextResolver resolver;").eol();
    }
    writer.eol();

    writer.append("  public %s$Route(%s %s", shortName, controllerType, controllerName);
    if (reader.isIncludeValidator()) {
      writer.append(", Validator validator");
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
    writer.append("  }").eol().eol();
  }
}
