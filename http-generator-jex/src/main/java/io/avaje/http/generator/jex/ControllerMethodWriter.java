package io.avaje.http.generator.jex;

import java.util.List;

import io.avaje.http.generator.core.*;

import javax.lang.model.type.TypeMirror;

import static io.avaje.http.generator.core.ProcessingContext.*;

/**
 * Write code to register Web route for a given controller method.
 */
class ControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;
  private final ControllerReader reader;
  private final WebMethod webMethod;
  private final boolean instrumentContext;
  private final boolean isFilter;

  ControllerMethodWriter(MethodReader method, Append writer, ControllerReader reader) {
    this.method = method;
    this.writer = writer;
    this.reader = reader;
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

    if (method.isErrorMethod()) {
      writer.append("    routing.error(%s.class, this::_%s)", method.exceptionShortName(), method.simpleName());
    } else if (isFilter) {
      writer.append("    routing.filter(this::_%s)", method.simpleName());
    } else {
      writer.append("    routing.%s(\"%s\", ", webMethod.name().toLowerCase(), fullPath);
      var hxRequest = method.hxRequest();
      if (hxRequest != null) {
        writeHxHandler(hxRequest);
      } else {
        writer.append("this::_%s)", method.simpleName());
      }
    }
    writeRoles();
    writer.append(";").eol();
  }

  private void writeRoles() {
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
  }

  private void writeHxHandler(HxRequestPrism hxRequest) {
    writer.append("HxHandler.builder(this::_%s)", method.simpleName());
    if (hasValue(hxRequest.target())) {
      writer.append(".target(\"%s\")", hxRequest.target());
    }
    if (hasValue(hxRequest.triggerId())) {
      writer.append(".trigger(\"%s\")", hxRequest.triggerId());
    } else if (hasValue(hxRequest.value())) {
      writer.append(".trigger(\"%s\")", hxRequest.value());
    }
    if (hasValue(hxRequest.triggerName())) {
      writer.append(".triggerName(\"%s\")", hxRequest.triggerName());
    } else if (hasValue(hxRequest.value())) {
      writer.append(".triggerName(\"%s\")", hxRequest.value());
    }
    writer.append(".build())");
  }

  private static boolean hasValue(String value) {
    return value != null && !value.isBlank();
  }

  enum ResponseMode {
    Void,
    Json,
    Text,
    Templating,
    InputStream,
    Other
  }

  ResponseMode responseMode() {
    if (method.isVoid() || isFilter) {
      return ResponseMode.Void;
    }
    if (isInputStream(method.returnType())) {
      return ResponseMode.InputStream;
    }
    if (producesJson()) {
      return ResponseMode.Json;
    }
    if (useTemplating()) {
      return ResponseMode.Templating;
    }
    if (producesText()) {
      return ResponseMode.Text;
    }
    return ResponseMode.Other;
  }

  private boolean isInputStream(TypeMirror type) {
    return isAssignable2Interface(type.toString(), "java.io.InputStream");
  }

  private boolean producesJson() {
    return // useJsonB
      !disabledDirectWrites()
      && !"byte[]".equals(method.returnType().toString())
      && (method.produces() == null || method.produces().toLowerCase().contains("json"));
  }

  private boolean producesText() {
    return  (method.produces() != null && method.produces().toLowerCase().contains("text"));
  }

  private boolean useContentCache() {
    return method.hasContentCache();
  }

  private boolean useTemplating() {
    return reader.html()
      && !"byte[]".equals(method.returnType().toString())
      && (method.produces() == null || method.produces().toLowerCase().contains("html"));
  }

  private boolean usesFormParams() {
    return method.params().stream().anyMatch(p -> p.isForm() || ParamType.FORMPARAM.equals(p.paramType()));
  }

  void writeHandler(boolean requestScoped) {
    if (method.isErrorMethod()) {
      writer.append("  private void _%s(Context ctx, %s ex) {", method.simpleName(), method.exceptionShortName());
    } else if (isFilter) {
      writer.append("  private void _%s(Context ctx, FilterChain chain) throws IOException {", method.simpleName());
    } else {
      writer.append("  private void _%s(Context ctx) throws IOException {", method.simpleName());
    }

    writer.eol();
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
    final var withFormParams = usesFormParams();
    final ResponseMode responseMode = responseMode();
    final boolean withContentCache = responseMode == ResponseMode.Templating && useContentCache();
    if (withContentCache) {
      writer.append("    var key = contentCache.key(ctx");
      if (withFormParams) {
        writer.append(", ctx.formParamMap()");
      }
      writer.append(");").eol();
      writer.append("    var cacheContent = contentCache.content(key);").eol();
      writer.append("    if (cacheContent != null) {").eol();
      writeContextReturn(responseMode);
      writer.append("      res.send(cacheContent);").eol();
      writer.append("      return;").eol();
      writer.append("    }").eol();
    }

    writer.append("    ");
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
    if (instrumentContext) {
      writer.append(")");
    }
    writer.append(";").eol();
    if (!method.isVoid()) {
      TypeMirror typeMirror = method.returnType();
      boolean includeNoContent = !typeMirror.getKind().isPrimitive();
      String indent = includeNoContent ? "      " : "    ";
      if (includeNoContent) {
        writer.append("    if (result != null) {").eol();
      }
      if (responseMode == ResponseMode.Templating) {
        writer.append(indent).append("var content = renderer.render(result);").eol();
        if (withContentCache) {
          writer.append(indent).append("contentCache.contentPut(key, content);").eol();
        }
        writer.append(indent);
        writeContextReturn(responseMode);
        writer.append("content);").eol();
      } else {
        writer.append(indent);
        writeContextReturn(responseMode);
        writer.append("result);").eol();
      }
      if (includeNoContent) {
        writer.append("    }").eol();
      }
    }
  }

  private void writeContextReturn(ResponseMode responseMode) {
    final var produces = method.produces();
    switch (responseMode) {
      case Void -> {}
      case Json -> writer.append("ctx.json(");
      case Text -> writer.append("ctx.text(");
      case Templating -> writer.append("ctx.html(");
      default -> writer.append("ctx.contentType(\"%s\").write(", produces);
    }
  }

  private static boolean isExceptionOrFilterChain(MethodParam param) {
    return isAssignable2Interface(param.utype().mainType(), "java.lang.Exception")
      || "FilterChain".equals(param.shortType());
  }
}
