package io.avaje.http.generator.jex;

import java.util.List;

import io.avaje.http.generator.core.*;
import io.avaje.http.generator.core.openapi.MediaType;

import javax.lang.model.type.TypeMirror;

import static io.avaje.http.generator.core.ProcessingContext.*;

/** Write code to register Web route for a given controller method. */
class ControllerMethodWriter {
  private final MethodReader method;
  private final Append writer;
  private final ControllerReader reader;
  private final WebMethod webMethod;
  private final boolean useJsonB;
  private final boolean useJstachio;
  private final boolean instrumentContext;
  private final boolean isFilter;

  ControllerMethodWriter(
      MethodReader method, Append writer, ControllerReader reader, boolean useJsonB) {
    this.method = method;
    this.writer = writer;
    this.reader = reader;
    this.useJstachio = ProcessingContext.isJstacheTemplate(method.returnType());
    this.useJsonB = !useJstachio && useJsonB;
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

  void writeRouting() {
    final PathSegments segments = method.pathSegments();
    String fullPath = segments.fullPath();
    if (fullPath.isEmpty()) {
      fullPath = "/";
    }

    if (method.isErrorMethod()) {
      writer.append("    routing.error(%s.class, this::_%s", method.exceptionShortName(), method.simpleName());
    } else if (isFilter) {
      writer.append("    routing.filter(this::_%s", method.simpleName());
    } else {
      writer.append("    routing.%s(\"%s\", ", webMethod.name().toLowerCase(), fullPath);
      var hxRequest = method.hxRequest();
      if (hxRequest != null) {
        writeHxHandler(hxRequest);
      } else {
        writer.append("this::_%s", method.simpleName());
      }
    }
    writeRoles();
    writer.append(");").eol();
  }

  private void writeRoles() {
    List<String> roles = method.roles();
    if (!roles.isEmpty() && !isFilter) {
      writer.append(", ");
      for (int i = 0; i < roles.size(); i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(Util.shortName(roles.get(i), true));
      }
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
    writer.append(".build()");
  }

  private static boolean hasValue(String value) {
    return value != null && !value.isBlank();
  }

  enum ResponseMode {
    Void,
    Json,
    Text,
    Jstachio,
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
    if (useJstachio) {
      return ResponseMode.Jstachio;
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
    return !"byte[]".equals(method.returnType().toString())
      && !useJstachio
      && (method.produces() == null || method.produces().toLowerCase().contains("json"));
  }

  private boolean producesText() {
    return (method.produces() != null && method.produces().toLowerCase().contains("text"));
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
      writer.append("  private void _%s(Context ctx, FilterChain chain) {", method.simpleName());
    } else {
      writer.append("  private void _%s(Context ctx) throws Exception {", method.simpleName());
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
      writeContextReturn(responseMode, "cacheContent", "");
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
      } else if ("HttpFilter.FilterChain".equals(param.shortType())) {
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
      switch (responseMode) {
        case Templating -> {
          writer.append(indent).append("var content = renderer.render(result);").eol();
          if (withContentCache) {
            writer.append(indent).append("contentCache.contentPut(key, content);").eol();
          }
          writeContextReturn(responseMode, "content", indent);
        }
        case Jstachio -> {
          var renderer = ProcessingContext.jstacheRenderer(method.returnType());
          writer.append(indent).append("var content = %s(result);", renderer).eol();
          writeContextReturn(responseMode, "content", indent);
        }
        default -> {
          writeContextReturn(responseMode, "result", indent);
        }
      }
      if (includeNoContent) {
        writer.append("    }").eol();
      }
    }
  }

  private void writeContextReturn(ResponseMode responseMode, String resultVariable, String indent) {
    writer.append(indent);
    final UType type = UType.parse(method.returnType());
    if ("java.util.concurrent.CompletableFuture".equals(type.mainType())) {
      logError(method.element(), "CompletableFuture is not a supported return type.");
      writer.append("; //ERROR").eol();
      return;
    }

    var produces = method.produces();
    if (produces == null && useJstachio) {
      writer.append("ctx.html(%s);", resultVariable).eol();
      return;
    }
    switch (responseMode) {
      case Void -> {}
      case Json -> writeJsonReturn(produces, indent);
      case Text -> writer.append("ctx.text(%s);", resultVariable);
      case Templating -> writer.append("ctx.html(%s);", resultVariable);
      default -> writer.append("ctx.contentType(\"%s\").write(%s);", produces, resultVariable);
    }
    writer.eol();
  }

  private void writeJsonReturn(String produces, String indent) {
    var uType = UType.parse(method.returnType());
    boolean streaming = useJsonB && streamingContent(uType);
    if (produces == null) {
      produces = streaming
        ? MediaType.APPLICATION_STREAM_JSON.getValue()
        : MediaType.APPLICATION_JSON.getValue();
    }
    if ("java.lang.String".equals(method.returnType().toString())) {
      writer.append("ctx.contentType(\"%s\").write(result); // raw json", produces);
      return;
    }
    if (useJsonB) {
      if (streaming) {
        writer.append("ctx.contentType(\"%s\");", produces).eol();
        writer.append(indent).append("%sJsonType.toJson(result, io.avaje.jex.core.json.JsonbOutput.of(ctx));", uType.shortName());
      } else {
        writer.append("ctx.jsonb(%sJsonType, result);", uType.shortName());
      }
    } else {
      writer.append("ctx.json(result);");
    }
  }

  private static boolean streamingContent(UType uType) {
    return uType.mainType().equals("java.util.stream.Stream");
  }

  private static boolean isExceptionOrFilterChain(MethodParam param) {
    return isAssignable2Interface(param.utype().mainType(), "java.lang.Exception")
      || "HttpFilter.FilterChain".equals(param.shortType());
  }
}
