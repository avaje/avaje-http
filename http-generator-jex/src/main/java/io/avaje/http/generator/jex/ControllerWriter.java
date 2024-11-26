package io.avaje.http.generator.jex;

import static io.avaje.http.generator.core.ProcessingContext.diAnnotation;
import io.avaje.http.generator.core.*;

import java.io.IOException;

/**
 * Write Jex specific Controller WebRoute handling adapter.
 */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-jex-generator\")";
  private static final String API_CONTEXT = "io.avaje.jex.Context";
  private static final String API_ROUTING = "io.avaje.jex.Routing";

  ControllerWriter(ControllerReader reader) throws IOException {
    super(reader);
    reader.addImportType(API_CONTEXT);
    reader.addImportType(API_ROUTING);
    reader.addImportType("java.io.IOException");

    if (reader.methods().stream()
        .map(MethodReader::webMethod)
        .anyMatch(w -> CoreWebMethod.FILTER == w)) {
      reader.addImportType("io.avaje.jex.FilterChain");
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
        new ControllerMethodWriter(method, writer).writeHandler(isRequestScoped());
        if (!reader.isDocHidden()) {
          method.buildApiDocumentation();
        }
      }
    }
  }

  private void writeRouting(MethodReader method) {
    new ControllerMethodWriter(method, writer).writeRouting();
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    writer.append(diAnnotation()).eol();
    writer.append("public class ").append(shortName).append("$Route implements Routing.HttpService {").eol().eol();

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
