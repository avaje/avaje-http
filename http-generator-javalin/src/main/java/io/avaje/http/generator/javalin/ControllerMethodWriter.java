package io.avaje.http.generator.javalin;

import java.util.List;

import io.avaje.http.api.MediaType;
import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PathSegments;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.http.generator.core.Util;
import io.avaje.http.generator.core.WebMethod;

/** Write code to register Web route for a given controller method. */
class ControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;
  private final ProcessingContext ctx;
  private final boolean useJsonB;

  ControllerMethodWriter(
      MethodReader method, Append writer, ProcessingContext ctx, boolean useJsonB) {
    this.method = method;
    this.writer = writer;
    webMethod = method.getWebMethod();
    this.ctx = ctx;
    this.useJsonB = useJsonB;
  }

  void write(boolean requestScoped) {

    final var segments = method.getPathSegments();
    final var fullPath = segments.fullPath();

    writer
        .append("    ApiBuilder.%s(\"%s\", ctx -> {", webMethod.name().toLowerCase(), fullPath)
        .eol();
    writer.append("      ctx.status(%s);", method.getStatusCode()).eol();

    final var matrixSegments = segments.matrixSegments();
    for (final PathSegments.Segment matrixSegment : matrixSegments) {
      matrixSegment.writeCreateSegment(writer, ctx.platform());
    }

    final var params = method.getParams();
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
      writeContextReturn();
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
    writer.append(")");
    if (!method.isVoid()) {
      writer.append(")");
    }
    writer.append(";").eol();
    writer.append("    }");

    final var roles = method.roles();
    if (!roles.isEmpty()) {
      writer.append(", ");
      for (var i = 0; i < roles.size(); i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(Util.shortName(roles.get(i)));
      }
    }
    writer.append(");").eol().eol();
  }

  private void writeContextReturn() {
    final var produces = method.getProduces();
    if (produces == null || MediaType.APPLICATION_JSON.equalsIgnoreCase(produces)) {
      writer.append("ctx.json(");
    } else if (MediaType.TEXT_HTML.equalsIgnoreCase(produces)) {
      writer.append("ctx.html(");
    } else if (MediaType.TEXT_PLAIN.equalsIgnoreCase(produces)) {
      writer.append("ctx.contentType(\"text/plain\").result(");
    } else {
      writer.append("ctx.contentType(\"%s\").result(", produces);
    }
  }

  private boolean producesJson() {
    return useJsonB
        && !"byte[]".equals(method.getReturnType().toString())
        && (method.getProduces() == null || method.getProduces().toLowerCase().contains("json"));
  }

  private boolean missingServerResponse(List<MethodParam> params) {
    return method.isVoid()
        && params.stream().noneMatch(p -> "ServerResponse".equals(p.getShortType()));
  }

  private boolean usesFormParams() {
    return method.getParams().stream()
        .anyMatch(p -> p.isForm() || ParamType.FORMPARAM.equals(p.getParamType()));
  }
}
