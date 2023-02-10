package io.avaje.http.generator.javalin;

import io.avaje.http.api.MediaType;
import io.avaje.http.generator.core.*;

/**
 * Write code to register Web route for a given controller method.
 */
class ControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;
  private final ProcessingContext ctx;
  private final boolean useJsonB;

  ControllerMethodWriter(MethodReader method, Append writer, ProcessingContext ctx, boolean useJsonB) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.ctx = ctx;
    this.useJsonB = useJsonB;
  }

  void write(boolean requestScoped) {

    final var segments = method.pathSegments();
    final var fullPath = segments.fullPath();

    writer.append("    ApiBuilder.%s(\"%s\", ctx -> {", webMethod.name().toLowerCase(), fullPath).eol();
    writer.append("      ctx.status(%s);", method.statusCode()).eol();

    final var matrixSegments = segments.matrixSegments();
    for (final PathSegments.Segment matrixSegment : matrixSegments) {
      matrixSegment.writeCreateSegment(writer, ctx.platform());
    }

    final var params = method.params();
    for (final MethodParam param : params) {
      param.writeCtxGet(writer, segments);
    }
    writer.append("      ");
    if (method.includeValidate()) {
      for (final MethodParam param : params) {
        param.writeValidate(writer);
      }
    }

    if (!method.isVoid()) {
      writer.append("var result = ");
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
      params.get(i).buildParamName(writer);
    }

    writer.append(");").eol();
    if (!method.isVoid()) {
      writeContextReturn();
      writer.eol();
    }

    writer.append("    }");

    final var roles = method.roles();
    if (!roles.isEmpty()) {
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

  private void writeContextReturn() {
    // Support for CompletableFuture's.
    final UType type = UType.parse(method.returnType());
    if (type.isGeneric() && type.mainType().equals("java.util.concurrent.CompletableFuture")) {
      final String returnVariableName = "futureResult";

      writer.append("      ctx.future(() -> {").eol();
      writer.append("        return result.thenAccept(%s -> {", returnVariableName).eol();
      writer.append("    ");
      this.writeContextReturn(returnVariableName);
      writer.eol().append("        });").eol();
      writer.append("      });").eol();
      return;
    }

    // Everything else
    this.writeContextReturn("result");
  }

  private void writeContextReturn(final String returnVariableName) {
    final var produces = method.produces();
    if (produces == null || MediaType.APPLICATION_JSON.equalsIgnoreCase(produces)) {
      if (useJsonB) {
        var uType = UType.parse(method.returnType());
        if (uType.isGeneric() && uType.mainType().equals("java.util.concurrent.CompletableFuture")) {
          uType = uType.paramRaw();
        }

        writer.append("      %sJsonType.toJson(%s, ctx.contentType(\"application/json\").outputStream());", uType.shortName(), returnVariableName);
      } else {
        writer.append("      ctx.json(%s);", returnVariableName);
      }
    } else if (MediaType.TEXT_HTML.equalsIgnoreCase(produces)) {
      writer.append("      ctx.html(%s);", returnVariableName);
    } else if (MediaType.TEXT_PLAIN.equalsIgnoreCase(produces)) {
      writer.append("      ctx.contentType(\"text/plain\").result(%s);", returnVariableName);
    } else {
      writer.append("      ctx.contentType(\"%s\").result(%s);", produces, returnVariableName);
    }
  }
}
