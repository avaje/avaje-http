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
  private final boolean isFilter;

  ControllerMethodWriter(MethodReader method, Append writer) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.instrumentContext = method.instrumentContext();
    this.isFilter = webMethod == CoreWebMethod.FILTER;
    if (isFilter) {
      validateFilter();
    }
  }

  private void validateFilter() {
    if (method.params().stream().map(MethodParam::shortType).noneMatch("HttpFilter.FilterChain"::equals)) {
      logError(method.element(), "Filters must contain a FilterChain parameter");
    }
  }

  void write() {
    final var segments = method.pathSegments();
    final var fullPath = segments.fullPath();

    writeMethod(fullPath);

    final var params = writeParams(segments);
    writer.append("      ");
    if (!method.isVoid() && !isFilter) {
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
      } else if ("HttpFilter.FilterChain".equals(param.shortType())) {
        writer.append("chain");
      } else {
        param.buildParamName(writer);
      }
    }

    if (instrumentContext) {
      writer.append(")");
    }

    writer.append(");").eol();
    if (!method.isVoid() && !isFilter) {
      writeContextReturn();
      writer.eol();
    }

    writer.append("    }");
    writer.append(");").eol().eol();
  }

  private void writeMethod(final String fullPath) {

    if (isFilter) {
      writer.append("    router.filter((ctx, chain) -> {");
    } else if (method.isErrorMethod()) {
      writer
          .append("    router.exception(%s.class, (ctx, ex) -> {", method.exceptionShortName())
          .eol();
    } else {
      var methodName = webMethod.name().toLowerCase().replace("_m", "M");
      writer.append("    router.%s(\"%s\", ctx -> {", methodName, fullPath).eol();
    }
    if (!isFilter) {
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
      if (!isExceptionOrFilterChain(param)) {
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

  private static boolean isExceptionOrFilterChain(MethodParam param) {
    return isAssignable2Interface(param.utype().mainType(), "java.lang.Exception")
      || "HttpFilter.FilterChain".equals(param.shortType());
  }
}
