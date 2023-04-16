package io.avaje.http.generator.jex;

import static io.avaje.http.generator.core.ProcessingContext.diAnnotation;
import io.avaje.http.generator.core.*;

import java.io.IOException;

/**
 * Write Jex specific Controller WebRoute handling adapter.
 */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-jex-generator\")";
  private static final String API_ROUTING = "io.avaje.jex.Routing";
  private static final String API_ROUTING_SERVICE = "io.avaje.jex.Routing.Service";

  ControllerWriter(ControllerReader reader) throws IOException {
    super(reader);
    reader.addImportType(API_ROUTING);
    reader.addImportType(API_ROUTING_SERVICE);
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
    writer.append("  public void add(Routing routing) {").eol().eol();
    for (MethodReader method : reader.methods()) {
      if (method.isWebMethod()) {
        writeForMethod(method);
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeForMethod(MethodReader method) {
    new ControllerMethodWriter(method, writer).write(isRequestScoped());
    if (!reader.isDocHidden()) {
      method.buildApiDocumentation();
    }
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    writer.append(diAnnotation()).eol();
    writer.append("public class ").append(shortName).append("$Route implements Routing.Service {").eol().eol();

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
