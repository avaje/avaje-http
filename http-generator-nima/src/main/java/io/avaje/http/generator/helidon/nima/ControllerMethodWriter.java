package io.avaje.http.generator.helidon.nima;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.avaje.http.api.MediaType;
import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PathSegments;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.http.generator.core.WebMethod;

/**
 * Write code to register Web route for a given controller method.
 */
class ControllerMethodWriter {

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;
  private final ProcessingContext ctx;
  private final boolean useJsonB;
  private final Map<String, JsonbType> jsonTypes;

  ControllerMethodWriter(
      MethodReader method,
      Append writer,
      ProcessingContext ctx,
      boolean useJsonB,
      Map<String, JsonbType> jsonTypes) {
    this.method = method;
    this.writer = writer;
    webMethod = method.getWebMethod();
    this.ctx = ctx;
    this.useJsonB = useJsonB;
    this.jsonTypes=jsonTypes;
  }

  void writeRule() {
    writer.append("    rules.%s(\"%s\", this::_%s);",
        webMethod.name().toLowerCase(), method.getFullPath(), method.simpleName())
      .eol();
  }

  void writeHandler(boolean requestScoped) {
    writer.append("  private void _%s(ServerRequest req, ServerResponse res) {", method.simpleName()).eol();
    final var bodyType = method.getBodyType();
    if (bodyType != null) {
      if (useJsonB) {

        final var jsonbType =
            method.getParams().stream()
                .filter(MethodParam::isBody)
                .findFirst()
                .orElseThrow()
                .getUType()
                .full()
                .transform(jsonTypes::get);
        writer
            .append(
                "    var %s = %sJsonType.fromJson(req.content().inputStream());",
                method.getBodyName(), jsonbType.fieldName())
            .eol();

      } else {
        // use default helidon content negotiation
        method.getParams().stream()
          .filter(MethodParam::isBody)
          .forEach(
            param -> {
              final var type = param.getUType();
              writer.append("    var %s = req.content().as(", method.getBodyName());
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

    final var segments = method.getPathSegments();
    if (!segments.isEmpty()) {
      writer.append("    var pathParams = req.path().pathParameters();").eol();
    }
    final var matrixSegments = segments.matrixSegments();
    for (final PathSegments.Segment matrixSegment : matrixSegments) {
      matrixSegment.writeCreateSegment(writer, ctx.platform());
    }

    final var params = method.getParams();
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

        final var jsonbType = method.getReturnType().toString().transform(jsonTypes::get);
        writer
            .append("    %sJsonType.toJson(result, res.outputStream());", jsonbType.fieldName())
            .eol();
      } else {
        writer.append("    res.send(result);").eol();
      }
    }
    writer.append("  }").eol().eol();
  }

  private boolean producesJson() {
    return useJsonB
      && !"byte[]".equals(method.getReturnType().toString())
      && (method.getProduces() == null || method.getProduces().toLowerCase().contains("json"));
  }

  private boolean missingServerResponse(List<MethodParam> params) {
    return method.isVoid() && params.stream().noneMatch(p -> "ServerResponse".equals(p.getShortType()));
  }

  private boolean usesFormParams() {
    return method.getParams().stream().anyMatch(p -> p.isForm() || ParamType.FORMPARAM.equals(p.getParamType()));
  }

  private void writeContextReturn() {
    final var producesOp = Optional.ofNullable(method.getProduces());
    if (producesOp.isEmpty() && !useJsonB) {
      return;
    }

    final var produces = producesOp.orElse(MediaType.APPLICATION_JSON);
    final var contentTypeString = "    res.headers().contentType(HttpMediaType.";
    switch (produces.toLowerCase()) {
      case MediaType.APPLICATION_JSON -> writer.append(contentTypeString + "APPLICATION_JSON);").eol();
      case MediaType.TEXT_HTML -> writer.append(contentTypeString + "TEXT_HTML);").eol();
      case MediaType.TEXT_PLAIN -> writer.append(contentTypeString + "TEXT_PLAIN);").eol();
      default -> writer.append(contentTypeString + "create(\"%s\"));", produces).eol();
    }
  }

  public void buildApiDocumentation() {
    method.buildApiDoc();
  }
}
