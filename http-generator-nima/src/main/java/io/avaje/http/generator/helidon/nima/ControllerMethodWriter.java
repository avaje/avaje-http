package io.avaje.http.generator.helidon.nima;

import java.util.List;
import java.util.Optional;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PathSegments;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.http.generator.core.UType;
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
  private final boolean useJsonB;

  ControllerMethodWriter(MethodReader method, Append writer, ProcessingContext ctx, boolean useJsonB) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.ctx = ctx;
    this.useJsonB = useJsonB;
  }

  void writeRule() {
    writer.append("    rules.%s(\"%s\", this::_%s);",
        webMethod.name().toLowerCase(), method.fullPath(), method.simpleName())
      .eol();
  }

  void writeHandler(boolean requestScoped) {
    writer.append("  private void _%s(ServerRequest req, ServerResponse res) {", method.simpleName()).eol();
    final var bodyType = method.bodyType();
    if (bodyType != null) {
      if (useJsonB) {
        final var fieldName =
            method.params().stream()
                .filter(MethodParam::isBody)
                .findFirst()
                .orElseThrow()
                .utype()
                .shortName();
        writer.append("    var %s = %sJsonType.fromJson(req.content().inputStream());", method.bodyName(), fieldName).eol();

      } else {
        // use default helidon content negotiation
        method.params().stream()
          .filter(MethodParam::isBody)
          .forEach(
            param -> {
              final var type = param.utype();
              writer.append("    var %s = req.content().as(", method.bodyName());
              if (type.param0() != null) {
                writer.append("new io.helidon.common.GenericType<%s>() {}", type.full());
              } else {
                writer.append("%s.class", type.full());
              }
              writer.append(");").eol();
            });
      }
    } else if (usesFormParams()) {
      writer.append("    var formParams = req.content().as(Parameters.class);").eol();
    }

    final var segments = method.pathSegments();
    if (!segments.isEmpty()) {
      writer.append("    var pathParams = req.path().pathParameters();").eol();
    }
    final var matrixSegments = segments.matrixSegments();
    for (final PathSegments.Segment matrixSegment : matrixSegments) {
      matrixSegment.writeCreateSegment(writer, ctx.platform());
    }

    final var params = method.params();
    for (final MethodParam param : params) {
      param.writeCtxGet(writer, segments);
    }
    writer.append("    ");
    if (!method.isVoid()) {
      writer.append("var result = ");
    } else if (missingServerResponse(params)) {
      throw new IllegalStateException("Void controller methods must have a ServerResponse parameter");
    }

    if (method.includeValidate()) {
      for (final MethodParam param : params) {
        param.writeValidate(writer);
      }
    }
    if (requestScoped) {
      writer.append("factory.create(req, res).");
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
      if (producesJson()) {
        final UType uType = UType.parse(method.returnType());
        writer.append("    %sJsonType.toJson(result, res.outputStream());", uType.shortName()).eol();
      } else {
        writer.append("    res.send(result);").eol();
      }
    }
    writer.append("  }").eol().eol();
  }

  private boolean producesJson() {
    return useJsonB
      && !"byte[]".equals(method.returnType().toString())
      && (method.produces() == null || method.produces().toLowerCase().contains("json"));
  }

  private boolean missingServerResponse(List<MethodParam> params) {
    return method.isVoid() && params.stream().noneMatch(p -> "ServerResponse".equals(p.shortType()));
  }

  private boolean usesFormParams() {
    return method.params().stream().anyMatch(p -> p.isForm() || ParamType.FORMPARAM.equals(p.paramType()));
  }

  private void writeContextReturn() {
    final var producesOp = Optional.ofNullable(method.produces());
    if (producesOp.isEmpty() && !useJsonB) {
      return;
    }

    final var produces = producesOp.map(MediaType::parse).orElse(MediaType.APPLICATION_JSON);
    final var contentTypeString = "    res.headers().contentType(HttpMediaType.";
    switch (produces) {
      case APPLICATION_JSON -> writer.append(contentTypeString + "APPLICATION_JSON);").eol();
      case TEXT_HTML -> writer.append(contentTypeString + "TEXT_HTML);").eol();
      case TEXT_PLAIN -> writer.append(contentTypeString + "TEXT_PLAIN);").eol();
      case UNKNOWN -> writer.append(contentTypeString + "create(\"%s\"));", produces).eol();
    }
  }

  public void buildApiDocumentation() {
    method.buildApiDoc();
  }
}
