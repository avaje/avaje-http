package io.avaje.http.generator.sigma;

import static io.avaje.http.generator.core.ProcessingContext.*;

import java.util.List;

import io.avaje.http.generator.core.*;
import io.avaje.http.generator.core.openapi.MediaType;

/** Write code to register Web route for a given controller method. */
class ControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;
  private final boolean instrumentContext;
  private final boolean customMethod;

  ControllerMethodWriter(MethodReader method, Append writer) {
    this.method = method;
    this.writer = writer;
    final var webM = method.webMethod();
    this.webMethod = webM == CoreWebMethod.FILTER ? JavalinWebMethod.BEFORE : webM;
    this.instrumentContext = method.instrumentContext();
    customMethod = !(webMethod instanceof CoreWebMethod);
  }

  void write() {
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

    writer.append("controller.");

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
      writer
          .append("    routing.exception(%s.class, (ex, ctx) -> {", method.exceptionShortName())
          .eol();
    } else {
      var methodName = webMethod.name().toLowerCase().replace("_m", "M");
      writer.append("    routing.%s(\"%s\", ctx -> {", methodName, fullPath).eol();
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
    var produces = method.produces();
    boolean applicationJson =
        produces == null || MediaType.APPLICATION_JSON.getValue().equalsIgnoreCase(produces);
    if (applicationJson || JsonBUtil.isJsonMimeType(produces)) {
      if (applicationJson) {
        writer.append("      ctx.json(result);");
      } else {
        writer.append("      ctx.contentType(\"%s\").result(result);", produces);
      }
    } else if (MediaType.TEXT_HTML.getValue().equalsIgnoreCase(produces)) {
      writer.append("      ctx.html(result);");
    } else if (MediaType.TEXT_PLAIN.getValue().equalsIgnoreCase(produces)) {
      writer.append("      ctx.text(result);");
    } else {
      writer.append("      ctx.contentType(\"%s\").result(%s);", produces);
    }
  }
}
