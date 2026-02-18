package io.avaje.http.generator.vertx;

import static io.avaje.http.generator.core.ProcessingContext.diAnnotation;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ParamType;
import java.io.IOException;

final class VertxControllerWriter extends BaseControllerWriter {

  private static final String GENERATED = "@Generated(\"avaje-vertx-generator\")";

  VertxControllerWriter(ControllerReader reader) throws IOException {
    super(reader);
    reader.addImportType("io.vertx.ext.web.Router");
    reader.addImportType("io.vertx.ext.web.RoutingContext");
    reader.addImportType("io.avaje.http.api.vertx.VertxRouteSet");
    reader.addImportType("io.avaje.http.api.Generated");
    reader.addImportType("io.vertx.core.json.Json");
    reader.addImportType("io.vertx.core.json.JsonArray");
    reader.addImportType("io.vertx.core.json.JsonObject");
    reader.addImportType("io.vertx.core.buffer.Buffer");
    if (hasBodyMethods()) {
      reader.addImportType("io.vertx.ext.web.handler.BodyHandler");
    }
    if (hasRoleProtectedMethods()) {
      reader.addImportType("io.vertx.ext.web.handler.AuthorizationHandler");
      reader.addImportType("io.vertx.ext.auth.authorization.RoleBasedAuthorization");
    }
    if (hasMethodWithMultipleRoles()) {
      reader.addImportType("io.vertx.ext.auth.authorization.OrAuthorization");
    }
  }

  void write() {
    writePackage();
    writeImports();
    writeClassStart();
    writeRegisterRoutes();
    writeClassEnd();
  }

  private void writeClassStart() {
    writer.append(GENERATED).eol();
    writer.append(diAnnotation()).eol();
    writer
      .append("public final class ")
      .append(shortName)
      .append("$Route implements VertxRouteSet {")
      .eol()
      .eol();

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

    writer.append("  private static String cookieValue(RoutingContext ctx, String cookieName) {").eol();
    writer.append("    final var cookie = ctx.request().getCookie(cookieName);").eol();
    writer.append("    return cookie != null ? cookie.getValue() : null;").eol();
    writer.append("  }").eol().eol();
  }

  private void writeRegisterRoutes() {
    writer.append("  @Override").eol();
    writer.append("  public void register(Router router) {").eol().eol();
    writer.append("    var routes = router;").eol().eol();

    for (MethodReader method : reader.methods()) {
      if (!method.isWebMethod()) {
        continue;
      }
      new VertxControllerMethodWriter(method, writer).write(isRequestScoped());
      if (!reader.isDocHidden()) {
        method.buildApiDocumentation();
      }
    }

    writer.append("  }").eol().eol();
  }

  private boolean hasRoleProtectedMethods() {
    for (MethodReader method : reader.methods()) {
      if (method.isWebMethod() && !method.roles().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  private boolean hasMethodWithMultipleRoles() {
    for (MethodReader method : reader.methods()) {
      if (method.isWebMethod() && method.roles().size() > 1) {
        return true;
      }
    }
    return false;
  }

  private boolean hasBodyMethods() {
    for (MethodReader method : reader.methods()) {
      if (!method.isWebMethod()) {
        continue;
      }
      for (var param : method.params()) {
        final var paramType = param.paramType();
        if (paramType == ParamType.BODY || paramType == ParamType.FORM || paramType == ParamType.FORMPARAM) {
          return true;
        }
      }
    }
    return false;
  }
}
