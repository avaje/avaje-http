package io.avaje.http.generator.vertx;

import static io.avaje.http.generator.core.ProcessingContext.diAnnotation;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.JsonBUtil;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PrimitiveUtil;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.http.generator.core.UType;

import java.io.IOException;
import java.util.Map;

final class VertxControllerWriter extends BaseControllerWriter {

  private static final String GENERATED = "@Generated(\"avaje-vertx-generator\")";
  private final boolean useJsonB;
  private final Map<String, UType> jsonTypes;

  VertxControllerWriter(ControllerReader reader) throws IOException {
    super(reader);

    final var detectJsonB = JsonBUtil.detect(ProcessingContext.useJsonb(), reader);
    this.useJsonB = detectJsonB.useJsonB();
    this.jsonTypes = detectJsonB.jsonTypes();
    reader.addStaticImportType("io.avaje.http.api.vertx.VertxUtils.cookieValue");
    reader.addImportType("io.vertx.ext.web.Router");
    reader.addImportType("io.vertx.ext.web.RoutingContext");
    reader.addImportType("io.avaje.http.api.vertx.VertxRouteSet");
    reader.addImportType("io.avaje.http.api.Generated");

    if (useJsonB) {
      reader.addImportType("io.vertx.core.buffer.Buffer");
    } else {
      reader.addImportType("io.vertx.core.json.Json");
    }

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

  private void writeRegisterRoutes() {
    writer.append("  @Override").eol();
    writer.append("  public void register(Router router) {").eol().eol();
    writer.append("    var routes = router;").eol().eol();

    for (MethodReader method : reader.methods()) {
      if (!method.isWebMethod()) {
        continue;
      }
      new VertxControllerMethodWriter(method, writer, useJsonB).write(isRequestScoped());
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
