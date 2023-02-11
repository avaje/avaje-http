package io.avaje.http.generator.jex;

import java.util.List;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.PathSegments;
import io.avaje.http.generator.core.ProcessingContext;
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
  private final ProcessingContext ctx;

  ControllerMethodWriter(MethodReader method, Append writer, ProcessingContext ctx) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.ctx = ctx;
  }

  void write(boolean requestScoped) {

    final PathSegments segments = method.pathSegments();
    final String fullPath = segments.fullPath();

    writer.append("    routing.%s(\"%s\", ctx -> {", webMethod.name().toLowerCase(), fullPath).eol();
    writer.append("      ctx.status(%s);", method.statusCode()).eol();

    List<PathSegments.Segment> matrixSegments = segments.matrixSegments();
    for (PathSegments.Segment matrixSegment : matrixSegments) {
      matrixSegment.writeCreateSegment(writer, ctx.platform());
    }

    final List<MethodParam> params = method.params();
    for (MethodParam param : params) {
      param.writeCtxGet(writer, segments);
    }
    writer.append("      ");
    if (method.includeValidate()) {
      for (MethodParam param : params) {
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
    for (int i = 0; i < params.size(); i++) {
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

    List<String> roles = method.roles();
    if (!roles.isEmpty()) {
      writer.append(").withRoles(");
      for (int i = 0; i < roles.size(); i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(Util.shortName(roles.get(i), true));
      }
    }
    writer.append(");").eol().eol();
  }

  private void writeContextReturn() {
    final var produces = method.produces();
    if (produces == null || produces.equalsIgnoreCase(MediaType.APPLICATION_JSON.getValue())) {
      writer.append("ctx.json(");
    } else if (produces.equalsIgnoreCase(MediaType.TEXT_HTML.getValue())) {
      writer.append("ctx.html(");
    } else if (produces.equalsIgnoreCase(MediaType.TEXT_PLAIN.getValue())) {
      writer.append("ctx.text(");
    } else {
      writer.append("ctx.contentType(\"%s\").write(", produces);
    }
  }
}
