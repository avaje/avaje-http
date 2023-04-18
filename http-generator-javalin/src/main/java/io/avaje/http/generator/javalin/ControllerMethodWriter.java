package io.avaje.http.generator.javalin;

import static io.avaje.http.generator.core.ProcessingContext.platform;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.PathSegments;
import io.avaje.http.generator.core.UType;
import io.avaje.http.generator.core.Util;
import io.avaje.http.generator.core.WebMethod;
import io.avaje.http.generator.core.openapi.MediaType;

/**
 * Write code to register Web route for a given controller method.
 */
class ControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;
  private final boolean useJsonB;
  private final boolean instrumentContext;

  ControllerMethodWriter(MethodReader method, Append writer, boolean useJsonB) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.useJsonB = useJsonB;
    this.instrumentContext = method.instrumentContext();
  }

  void write(boolean requestScoped) {
    final var segments = method.pathSegments();
    final var fullPath = segments.fullPath();

    writer.append("    ApiBuilder.%s(\"%s\", ctx -> {", webMethod.name().toLowerCase(), fullPath).eol();
    writer.append("      ctx.status(%s);", method.statusCode()).eol();

    final var matrixSegments = segments.matrixSegments();
    for (final PathSegments.Segment matrixSegment : matrixSegments) {
      matrixSegment.writeCreateSegment(writer, platform());
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
      params.get(i).buildParamName(writer);
    }

    if (instrumentContext) {
      writer.append(")");
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
    if ("java.util.concurrent.CompletableFuture".equals(type.mainType())) {
      if (!type.isGeneric()) {
        throw new IllegalStateException("CompletableFuture must be generic type (e.g. CompletableFuture<String>, CompletableFuture<Void>).");
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
    final var produces = method.produces();
    if (produces == null || MediaType.APPLICATION_JSON.getValue().equalsIgnoreCase(produces)) {
      if (useJsonB) {
        var uType = UType.parse(method.returnType());
        if ("java.util.concurrent.CompletableFuture".equals(uType.mainType())) {
          uType = uType.paramRaw();
        }
        writer.append("      %sJsonType.toJson(%s, ctx.contentType(\"application/json\").outputStream());", uType.shortName(), resultVariableName);
      } else {
        writer.append("      ctx.json(%s);", resultVariableName);
      }
    } else if (MediaType.TEXT_HTML.getValue().equalsIgnoreCase(produces)) {
      writer.append("      ctx.html(%s);", resultVariableName);
    } else if (MediaType.TEXT_PLAIN.getValue().equalsIgnoreCase(produces)) {
      writer.append("      ctx.contentType(\"text/plain\").result(%s);", resultVariableName);
    } else {
      writer.append("      ctx.contentType(\"%s\").result(%s);", produces, resultVariableName);
    }
  }
}
