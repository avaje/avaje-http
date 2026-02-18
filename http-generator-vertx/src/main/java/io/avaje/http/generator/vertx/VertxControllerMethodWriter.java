package io.avaje.http.generator.vertx;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.CoreWebMethod;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PathSegments;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.http.generator.core.UType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

final class VertxControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;

  VertxControllerMethodWriter(MethodReader method, Append writer) {
    this.method = method;
    this.writer = writer;
  }

  void write(boolean requestScoped) {
    if (!(method.webMethod() instanceof CoreWebMethod coreWebMethod)) {
      return;
    }
    switch (coreWebMethod) {
      case FILTER:
        writeFilterRoute(requestScoped);
        return;
      case ERROR:
        writeErrorRoute(requestScoped);
        return;
      default:
        writeHttpRoute(coreWebMethod, requestScoped);
    }
  }

  private void writeHttpRoute(CoreWebMethod coreWebMethod, boolean requestScoped) {
    final String routeMethod = routeMethod(coreWebMethod);
    if (routeMethod == null) {
      return;
    }

    final String fullPath = toVertxPath(method.pathSegments().fullPath());
    writer.append("    {").eol();
    writer.append("      var route = routes.%s(\"%s\");", routeMethod, fullPath).eol();
    if (requiresBodyHandler()) {
      writer.append("      route.handler(BodyHandler.create());").eol();
    }
    writeRoleHandlers();
    writer.append("      route.handler(ctx -> {").eol();
    writer.append("      try {").eol();

    int statusCode = method.statusCode();
    if (statusCode > 0) {
      writer.append("        ctx.response().setStatusCode(%d);", statusCode).eol();
    }

    final List<MethodParam> params = writeParams(method.pathSegments());
    writeControllerCall(params, requestScoped, "e", true);

    if (!method.isVoid()) {
      writeReturn("result", UType.parse(method.returnType()));
    }

    writer.append("      } catch (Throwable e) {").eol();
    writer.append("        ctx.fail(e);").eol();
    writer.append("      }").eol();
    writer.append("      });").eol().eol();
    writer.append("    }").eol().eol();
  }

  private void writeFilterRoute(boolean requestScoped) {
    final String fullPath = toVertxPath(method.pathSegments().fullPath());
    writer.append("    {").eol();
    writer.append("      io.vertx.core.Handler<RoutingContext> filterHandler = ctx -> {").eol();
    writer.append("      try {").eol();

    final List<MethodParam> params = writeParams(method.pathSegments());
    writeControllerCall(params, requestScoped, null, false);
    writer.append("        if (!ctx.response().ended() && !ctx.failed()) {").eol();
    writer.append("          ctx.next();").eol();
    writer.append("        }").eol();

    writer.append("      } catch (Throwable e) {").eol();
    writer.append("        ctx.fail(e);").eol();
    writer.append("      }").eol();
    writer.append("      };").eol();
    writeScopedHandlerRegistration(fullPath, "handler", "filterHandler");
    writer.append("    }").eol().eol();
  }

  private void writeErrorRoute(boolean requestScoped) {
    final String fullPath = toVertxPath(method.pathSegments().fullPath());
    writer.append("    {").eol();
    writer.append("      io.vertx.core.Handler<RoutingContext> errorHandler = ctx -> {").eol();
    writer.append("      var failure = ctx.failure();").eol();
    writer.append("      if (failure == null || !(failure instanceof %s)) {", method.exceptionShortName()).eol();
    writer.append("        ctx.next();").eol();
    writer.append("        return;").eol();
    writer.append("      }").eol();
    writer.append("      var ex = (%s) failure;", method.exceptionShortName()).eol();
    writer.append("      try {").eol();

    final int statusCode = method.statusCode();
    if (statusCode > 0) {
      writer.append("        ctx.response().setStatusCode(%d);", statusCode).eol();
    }

    final List<MethodParam> params = writeParams(method.pathSegments());
    writeControllerCall(params, requestScoped, "ex", true);
    if (!method.isVoid()) {
      writeReturn("result", UType.parse(method.returnType()));
    }

    writer.append("      } catch (Throwable e) {").eol();
    writer.append("        ctx.fail(e);").eol();
    writer.append("      }").eol();
    writer.append("      };").eol();
    writeScopedHandlerRegistration(fullPath, "failureHandler", "errorHandler");
    writer.append("    }").eol().eol();
  }

  private void writeScopedHandlerRegistration(String fullPath, String registerMethod, String handlerName) {
    if ("/".equals(fullPath)) {
      writer.append("      routes.route().%s(%s);", registerMethod, handlerName).eol();
      return;
    }
    writer
      .append("      routes.route(\"%s\").%s(%s);", escapeJava(fullPath), registerMethod, handlerName)
      .eol();
    writer
      .append("      routes.route(\"%s/*\").%s(%s);", escapeJava(fullPath), registerMethod, handlerName)
      .eol();
  }

  private void writeControllerCall(
      List<MethodParam> params,
      boolean requestScoped,
      String exceptionVar,
      boolean captureResult
  ) {
    writer.append("        ");
    if (captureResult && !method.isVoid()) {
      writer.append("Object result = ");
    }
    if (requestScoped) {
      writer.append("factory.create(ctx).");
    } else {
      writer.append("controller.");
    }
    writer.append(method.simpleName()).append("(");
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      final MethodParam param = params.get(i);
      if (ProcessingContext.isAssignable2Interface(param.utype().mainType(), "java.lang.Exception")) {
        if (exceptionVar == null) {
          writer.append("null");
        } else {
          writer.append(exceptionVar);
        }
      } else {
        param.buildParamName(writer);
      }
    }
    writer.append(");").eol();
  }

  private List<MethodParam> writeParams(PathSegments segments) {
    for (PathSegments.Segment matrixSegment : segments.matrixSegments()) {
      matrixSegment.writeCreateSegment(writer, ProcessingContext.platform());
    }

    List<MethodParam> params = method.params();
    for (MethodParam param : params) {
      if (!ProcessingContext.isAssignable2Interface(param.utype().mainType(), "java.lang.Exception")) {
        param.writeCtxGet(writer, segments);
      }
    }
    if (method.includeValidate()) {
      for (MethodParam param : params) {
        param.writeValidate(writer);
      }
    }
    return params;
  }

  private void writeReturn(String resultVar, UType returnType) {
    final String mainType = returnType.mainType();
    if ("java.util.concurrent.CompletionStage".equals(mainType)
      || "java.util.concurrent.CompletableFuture".equals(mainType)) {
      writer.append("        var asyncResult = (java.util.concurrent.CompletionStage<?>) %s;", resultVar).eol();
      writer.append("        if (asyncResult == null) {").eol();
      writer.append("          return;").eol();
      writer.append("        }").eol();
      writer.append("        asyncResult.whenComplete((value, error) -> {").eol();
      writer.append("          if (error != null) {").eol();
      writer.append("            ctx.fail(error);").eol();
      writer.append("            return;").eol();
      writer.append("          }").eol();
      writeReturnValue("value", returnType.paramRaw());
      writer.append("        });").eol();
      return;
    }
    if ("io.vertx.core.Future".equals(mainType)) {
      writer.append("        var asyncResult = (io.vertx.core.Future<?>) %s;", resultVar).eol();
      writer.append("        if (asyncResult == null) {").eol();
      writer.append("          return;").eol();
      writer.append("        }").eol();
      writer.append("        asyncResult.onFailure(ctx::fail);").eol();
      writer.append("        asyncResult.onSuccess(value -> {").eol();
      writeReturnValue("value", returnType.paramRaw());
      writer.append("        });").eol();
      return;
    }
    writeReturnValue(resultVar, returnType);
  }

  private void writeReturnValue(String valueVar, UType returnType) {
    final String produces = method.produces();
    final String contentType = normalizeContentType(produces);
    writer.append("          if (%s == null || ctx.response().ended()) {", valueVar).eol();
    writer.append("            return;").eol();
    writer.append("          }").eol();

    writer.append("          if (%s instanceof Buffer buffer) {", valueVar).eol();
    if (contentType != null) {
      writer.append("            ctx.response().putHeader(\"content-type\", \"%s\");", escapeJava(contentType)).eol();
    }
    writer.append("            ctx.response().end(buffer);").eol();
    writer.append("            return;").eol();
    writer.append("          }").eol();

    writer.append("          if (%s instanceof JsonObject jsonObject) {", valueVar).eol();
    writer.append("            ctx.response().putHeader(\"content-type\", \"application/json\");").eol();
    writer.append("            ctx.response().end(jsonObject.encode());").eol();
    writer.append("            return;").eol();
    writer.append("          }").eol();

    writer.append("          if (%s instanceof JsonArray jsonArray) {", valueVar).eol();
    writer.append("            ctx.response().putHeader(\"content-type\", \"application/json\");").eol();
    writer.append("            ctx.response().end(jsonArray.encode());").eol();
    writer.append("            return;").eol();
    writer.append("          }").eol();

    writer.append("          if (%s instanceof String stringValue) {", valueVar).eol();
    if (contentType == null) {
      writer.append("            ctx.response().putHeader(\"content-type\", \"text/plain\");").eol();
    } else {
      writer.append("            ctx.response().putHeader(\"content-type\", \"%s\");", escapeJava(contentType)).eol();
    }
    writer.append("            ctx.response().end(stringValue);").eol();
    writer.append("            return;").eol();
    writer.append("          }").eol();

    if (contentType != null) {
      writer.append("          ctx.response().putHeader(\"content-type\", \"%s\");", escapeJava(contentType)).eol();
      if (contentType.startsWith("text/")) {
        writer.append("          ctx.response().end(String.valueOf(%s));", valueVar).eol();
      } else {
        writer.append("          ctx.response().end(Json.encode(%s));", valueVar).eol();
      }
    } else if (isStringLike(returnType)) {
      writer.append("          ctx.response().putHeader(\"content-type\", \"text/plain\");").eol();
      writer.append("          ctx.response().end(String.valueOf(%s));", valueVar).eol();
    } else {
      writer.append("          ctx.response().putHeader(\"content-type\", \"application/json\");").eol();
      writer.append("          ctx.response().end(Json.encode(%s));", valueVar).eol();
    }
  }

  private boolean isStringLike(UType returnType) {
    final String mainType = returnType.mainType();
    return "java.lang.String".equals(mainType)
      || "char".equals(mainType)
      || "java.lang.Character".equals(mainType)
      || "byte".equals(mainType)
      || "short".equals(mainType)
      || "int".equals(mainType)
      || "long".equals(mainType)
      || "float".equals(mainType)
      || "double".equals(mainType)
      || "boolean".equals(mainType)
      || "java.lang.Byte".equals(mainType)
      || "java.lang.Short".equals(mainType)
      || "java.lang.Integer".equals(mainType)
      || "java.lang.Long".equals(mainType)
      || "java.lang.Float".equals(mainType)
      || "java.lang.Double".equals(mainType)
      || "java.lang.Boolean".equals(mainType);
  }

  private String normalizeContentType(String produces) {
    if (produces == null) {
      return null;
    }
    final String trimmed = produces.trim();
    if (trimmed.isBlank()) {
      return null;
    }
    return trimmed;
  }

  private String routeMethod(CoreWebMethod webMethod) {
    switch (webMethod) {
      case GET:
        return "get";
      case POST:
        return "post";
      case PUT:
        return "put";
      case PATCH:
        return "patch";
      case DELETE:
        return "delete";
      case OPTIONS:
        return "options";
      default:
        return null;
    }
  }

  private String toVertxPath(String path) {
    String normalized = path;
    if (normalized == null || normalized.isBlank()) {
      return "/";
    }
    if (!normalized.startsWith("/")) {
      normalized = "/" + normalized;
    }
    return normalized.replaceAll("\\{([^}/]+)}", ":$1");
  }

  private void writeRoleHandlers() {
    List<String> roles = method.roles();
    if (roles == null || roles.isEmpty()) {
      return;
    }
    List<String> uniqueRoles = new ArrayList<>(new LinkedHashSet<>(roles));
    if (uniqueRoles.size() == 1) {
      writer.append(
        "      route.handler(AuthorizationHandler.create(RoleBasedAuthorization.create(\"%s\")));",
        escapeJava(uniqueRoles.get(0))
      ).eol();
      return;
    }

    writer.append("      route.handler(AuthorizationHandler.create(OrAuthorization.create()").eol();
    for (String role : uniqueRoles) {
      writer.append("        .addAuthorization(RoleBasedAuthorization.create(\"%s\"))", escapeJava(role)).eol();
    }
    writer.append("      ));").eol();
  }

  private boolean requiresBodyHandler() {
    for (MethodParam param : method.params()) {
      final ParamType paramType = param.paramType();
      if (paramType == ParamType.BODY || paramType == ParamType.FORM || paramType == ParamType.FORMPARAM) {
        return true;
      }
    }
    return false;
  }

  private String escapeJava(String value) {
    if (value == null) {
      return "";
    }
    return value
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t");
  }
}
