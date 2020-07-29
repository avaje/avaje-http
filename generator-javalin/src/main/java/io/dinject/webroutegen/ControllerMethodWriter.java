package io.dinject.webroutegen;

import io.dinject.controller.MediaType;

import java.util.List;

/**
 * Write code to register Web route for a given controller method.
 */
class ControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;

  ControllerMethodWriter(MethodReader method, Append writer) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.getWebMethod();
  }

  void write() {

    final PathSegments segments = method.getPathSegments();
    final String fullPath = segments.fullPath();

    writer.append("    ApiBuilder.%s(\"%s\", ctx -> {", webMethod.name().toLowerCase(), fullPath).eol();
    writer.append("      ctx.status(%s);", method.getStatusCode()).eol();

    List<PathSegments.Segment> metricSegments = segments.metricSegments();
    for (PathSegments.Segment metricSegment : metricSegments) {
      metricSegment.writeCreateSegment(writer);
    }

    final List<MethodParam> params = method.getParams();
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

    writer.append("controller.");
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
      writer.append(", roles(");
      for (int i = 0; i < roles.size(); i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(Util.shortName(roles.get(i)));
      }
      writer.append(")");
    }
    writer.append(");").eol().eol();
  }

  private void writeContextReturn() {
    final String produces = method.getProduces();
    if (produces == null || produces.equalsIgnoreCase(MediaType.APPLICATION_JSON)) {
      writer.append("ctx.json(");
    } else if (produces.equalsIgnoreCase(MediaType.TEXT_HTML)) {
      writer.append("ctx.html(");
    } else if (produces.equalsIgnoreCase(MediaType.TEXT_PLAIN)) {
      writer.append("ctx.contentType(\"text/plain\").result(");
    } else {
      writer.append("ctx.contentType(\"%s\").result(", produces);
    }
  }
}
