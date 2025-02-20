package io.avaje.http.generator.javalin;

import static io.avaje.http.generator.core.ProcessingContext.*;

import java.util.List;

import io.avaje.http.generator.core.*;
import io.avaje.http.generator.core.openapi.MediaType;

/** Write code to register Web route for a given controller method. */
class ControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;
  private final boolean useJsonB;
  private final boolean instrumentContext;
  private final boolean customMethod;
  private final boolean useJstachio;

  ControllerMethodWriter(MethodReader method, Append writer, boolean useJsonB) {
    this.method = method;
    this.writer = writer;
    final var webM = method.webMethod();
    this.webMethod = webM == CoreWebMethod.FILTER ? JavalinWebMethod.BEFORE : webM;
    this.useJstachio = ProcessingContext.isJstacheTemplate(method.returnType());
    this.useJsonB = !useJstachio && useJsonB && !disabledDirectWrites();
    this.instrumentContext = method.instrumentContext();
    customMethod = !(webMethod instanceof CoreWebMethod);
  }

  void write(boolean requestScoped) {
    final var segments = method.pathSegments();
    final var fullPath = segments.fullPath();

    writeMethod(fullPath);

    final var params = writeParams(segments);
    writer.append("      ");
    if (!method.isVoid() && !customMethod) {
      writer.append("var result = ");
    }

    if (instrumentContext) {
      method.writeContext(writer, "ctx", "ctx");
    }

    if (requestScoped) {
      writer.append("factory.create(ctx).");
    } else {
      writer.append("controller.");
    }

    writer.append(method.simpleName()).append("(");
    for (var i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      final var param = params.get(i);
      if (isAssignable2Interface(param.utype().mainType(), "java.lang.Exception")) {
        writer.append("ex");
      } else {
        param.buildParamName(writer);
      }
    }

    if (instrumentContext) {
      writer.append(")");
    }

    writer.append(");").eol();
    if (!method.isVoid() && !customMethod) {
      writeContextReturn();
      writer.eol();
    }

    writer.append("    }");

    final var roles = method.roles();
    if (!roles.isEmpty() && !customMethod) {
      writer.append(", ");
      for (var i = 0; i < roles.size(); i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(Util.shortName(roles.get(i), true));
      }
    }
    writer.append(");").eol().eol();
  }

  private void writeMethod(final String fullPath) {
    if (method.isErrorMethod()) {
      writer.append("    app.exception(%s.class, (ex, ctx) -> {", method.exceptionShortName()).eol();
    } else {
      var methodName = webMethod.name().toLowerCase().replace("_m", "M");
      writer.append("    app.%s(\"%s\", ctx -> {", methodName, fullPath).eol();
    }
    if (!customMethod) {
      int statusCode = method.statusCode();
      if (statusCode > 0) {
        writer.append("      ctx.status(%d);", statusCode).eol();
      }
    }
  }

  private List<MethodParam> writeParams(final PathSegments segments) {
    final var matrixSegments = segments.matrixSegments();
    for (final PathSegments.Segment matrixSegment : matrixSegments) {
      matrixSegment.writeCreateSegment(writer, platform());
    }

    final var params = method.params();
    for (final MethodParam param : params) {
      if (!isAssignable2Interface(param.utype().mainType(), "java.lang.Exception")) {
        param.writeCtxGet(writer, segments);
      }
    }
    if (method.includeValidate()) {
      for (final MethodParam param : params) {
        param.writeValidate(writer);
      }
    }
    return params;
  }

  private void writeContextReturn() {
    if (instrumentContext) {
      writer.append("      if (ctx.resultInputStream() != null || ctx.res().isCommitted()) return;").eol();
    }
    // Support for CompletableFuture's.
    final UType type = UType.parse(method.returnType());
    if ("java.util.concurrent.CompletableFuture".equals(type.mainType())) {
      if (!type.isGeneric()) {
        logError(
            method.element(),
            "CompletableFuture must be generic type (e.g. CompletableFuture<String>, CompletableFuture<Void>).");
      }

      final String futureResultVariableName = "futureResult";

      writer.append("      ctx.future(() -> {").eol();
      writer.append("        return result.thenAccept(%s -> {", futureResultVariableName).eol();
      writer.append("    ");
      this.writeContextReturn(futureResultVariableName);
      writer.eol().append("        });").eol();
      writer.append("      });");
      return;
    }

    // Everything else
    this.writeContextReturn("result");
  }

  private void writeContextReturn(final String resultVariableName) {
    var produces = method.produces();

    if (useJstachio && produces == null) {
      produces = MediaType.TEXT_HTML.getValue();
    }

    boolean applicationJson = produces == null || MediaType.APPLICATION_JSON.getValue().equalsIgnoreCase(produces);
    if (applicationJson || JsonBUtil.isJsonMimeType(produces)) {
      if (useJsonB) {
        var uType = UType.parse(method.returnType());
        final boolean isfuture = "java.util.concurrent.CompletableFuture".equals(uType.mainType());
        if (isfuture || method.isErrorMethod()) {
          if (isfuture) {
            uType = uType.paramRaw();
          }
          writer.append("      try {");
        }
        if (produces == null) {
          produces = MediaType.APPLICATION_JSON.getValue();
        }
        writer.append("      %sJsonType.toJson(%s, ctx.contentType(\"%s\").res().getOutputStream());", uType.shortName(), resultVariableName, produces);
        if (isfuture || method.isErrorMethod()) {
          writer.append("      } catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }");
        }
      } else if (applicationJson) {
        writer.append("      ctx.json(%s);", resultVariableName);
      } else {
        writer.append("      ctx.contentType(\"%s\").json(%s);", produces, resultVariableName);
      }
    } else if (useJstachio) {
      var renderer = ProcessingContext.jstacheRenderer(method.returnType());
      writer.append("      ctx.contentType(\"%s\").result(%s(%s));", produces, renderer, resultVariableName);
    } else if (MediaType.TEXT_HTML.getValue().equalsIgnoreCase(produces)) {
      writer.append("      ctx.html(%s);", resultVariableName);
    } else if (MediaType.TEXT_PLAIN.getValue().equalsIgnoreCase(produces)) {
      writer.append("      ctx.contentType(\"text/plain\").result(%s);", resultVariableName);
    } else {
      writer.append("      ctx.contentType(\"%s\").result(%s);", produces, resultVariableName);
    }
  }
}
