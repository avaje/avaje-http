package io.avaje.http.generator.jex;

import static io.avaje.http.generator.core.ProcessingContext.isAssignable2Interface;
import static io.avaje.http.generator.core.ProcessingContext.logError;
import static io.avaje.http.generator.core.ProcessingContext.platform;

import java.io.IOException;
import java.util.List;

import io.avaje.http.generator.core.*;
import io.avaje.http.generator.core.openapi.MediaType;

/**
 * Write code to register Web route for a given controller method.
 */
class ControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;
  private final boolean instrumentContext;
  private final boolean isFilter;

  ControllerMethodWriter(MethodReader method, Append writer) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.instrumentContext = method.instrumentContext();
    this.isFilter = webMethod == CoreWebMethod.FILTER;
    if (isFilter) {
      validateMethod();
    }
  }

  private void validateMethod() {
    if (method.params().stream().map(MethodParam::shortType).noneMatch("FilterChain"::equals)) {
      logError(method.element(), "Filters must contain a FilterChain parameter");
    }
  }

  void writeRouting() {
    final PathSegments segments = method.pathSegments();
    final String fullPath = segments.fullPath();
    writer.append("    routing.%s(\"%s\", this::_%s)", webMethod.name().toLowerCase(), fullPath, method.simpleName());
    List<String> roles = method.roles();
    if (!roles.isEmpty()) {
      writer.append(".withRoles(");
      for (int i = 0; i < roles.size(); i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(Util.shortName(roles.get(i), true));
      }
      writer.append(")");
    }
    writer.append(";").eol();
  }

  void writeHandler(boolean requestScoped) {

    if (method.isErrorMethod()) {
      writer.append("  private void _%s(Context ctx, %s ex)", method.simpleName(), method.exceptionShortName()).eol();
    } else if (isFilter) {
      writer.append("  private void _%s(Context ctx, FilterChain chain)", method.simpleName()).eol();
    } else {
      writer.append("  private void _%s(Context ctx)", method.simpleName()).eol();
    }

    writer.append(" throws IOException", method.simpleName()).eol();

    write(requestScoped);
    writer.append("  }").eol().eol();
  }

  private void write(boolean requestScoped) {
    int statusCode = method.statusCode();
    if (statusCode > 0) {
      writer.append("    ctx.status(%d);", statusCode).eol();
    }

    final PathSegments segments = method.pathSegments();
    List<PathSegments.Segment> matrixSegments = segments.matrixSegments();
    for (PathSegments.Segment matrixSegment : matrixSegments) {
      matrixSegment.writeCreateSegment(writer, platform());
    }

    final List<MethodParam> params = method.params();
    for (MethodParam param : params) {
      if (!isExceptionOrFilterChain(param)) {
        param.writeCtxGet(writer, segments);
      }
    }
    if (method.includeValidate()) {
      for (MethodParam param : params) {
        param.writeValidate(writer);
      }
    }
    writer.append("    ");
    if (!method.isVoid()) {
      writeContextReturn();
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
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      final var param = params.get(i);
      if (isAssignable2Interface(param.utype().mainType(), "java.lang.Exception")) {
        writer.append("ex");
      } else if ("FilterChain".equals(param.shortType())) {
        writer.append("chain");
      } else {
        param.buildParamName(writer);
      }
    }
    writer.append(")");
    if (!method.isVoid()) {
      writer.append(")");
    }
    if (instrumentContext) {
      writer.append(")");
    }
    writer.append(";").eol();
  }

  private void writeContextReturn() {
    final var produces = method.produces();
    if (produces == null || produces.equalsIgnoreCase(MediaType.APPLICATION_JSON.getValue())) {
      writer.append("ctx.json(");
    } else if (produces.equalsIgnoreCase(MediaType.TEXT_HTML.getValue())) {
      writer.append("ctx.html(");
    } else if (produces.equalsIgnoreCase(MediaType.TEXT_PLAIN.getValue())) {
      writer.append("ctx.text(");
    } else if (JsonBUtil.isJsonMimeType(produces)) {
      writer.append("ctx.contentType(\"%s\").json(", produces);
    } else {
      writer.append("ctx.contentType(\"%s\").write(", produces);
    }
  }

  private static boolean isExceptionOrFilterChain(MethodParam param) {
    return isAssignable2Interface(param.utype().mainType(), "java.lang.Exception")
      || "FilterChain".equals(param.shortType());
  }
}
