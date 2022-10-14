package io.avaje.http.generator.helidon.nima;

import io.avaje.http.api.MediaType;
import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
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

  ControllerMethodWriter(MethodReader method, Append writer, ProcessingContext ctx) {
    this.method = method;
    this.writer = writer;
    webMethod = method.getWebMethod();
    this.ctx = ctx;
  }

  void writeRule() {
    final var fullPath = method.getFullPath();
//    final String bodyType = method.getBodyType();
//    if (bodyType != null) {
//      writer.append("    rules.%s(\"%s\", Handler.create(%s.class, this::_%s));", webMethod.name().toLowerCase(), fullPath, bodyType, method.simpleName()).eol();
//    } else if (method.isFormBody()) {
//      writer.append("    rules.%s(\"%s\", Handler.create(%s.class, this::_%s));", webMethod.name().toLowerCase(), fullPath, "FormParams", method.simpleName()).eol();
//    } else {
      writer.append("    rules.%s(\"%s\", this::_%s);", webMethod.name().toLowerCase(), fullPath, method.simpleName()).eol();
//    }
  }

  void writeHandler(boolean requestScoped) {
    writer.append("  private void _%s(ServerRequest req, ServerResponse res", method.simpleName());

    writer.append(") {").eol();
//    if (!method.isVoid()) {
//      writeContextReturn();
//    }

    final var bodyType = method.getBodyType();
    if (bodyType != null) {
      writer.append("    // body - %s %s", bodyType, method.getBodyName()).eol();
    }// else if (method.isFormBody()) {
    //  writer.append(", %s %s", "FormParams", "formParams");
    //}

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
      //writer.append("res.send(");
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
      writer.append("    res.send(result);").eol();
    }
    writer.append("  }").eol().eol();
  }

  private void writeContextReturn() {
    final var produces = method.getProduces();

    if (produces == null) {
      return;
    }

    final var contentTypeString =
        "    res.headers().contentType(io.helidon.common.http.HttpMediaType.";

    switch (produces.toLowerCase()) {
      case MediaType.APPLICATION_JSON -> writer
          .append(contentTypeString + "APPLICATION_JSON);")
          .eol();
      case MediaType.TEXT_HTML -> writer.append(contentTypeString + "TEXT_HTML);").eol();
      case MediaType.TEXT_PLAIN -> writer.append(contentTypeString + "TEXT_PLAIN);").eol();
      default -> writer.append(contentTypeString + "create(\"%s\"));", produces).eol();
    }
  }

  public void buildApiDocumentation() {
    method.buildApiDoc();
  }
}
