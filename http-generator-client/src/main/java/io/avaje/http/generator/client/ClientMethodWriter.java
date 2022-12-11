package io.avaje.http.generator.client;

import java.util.Set;

import javax.lang.model.element.TypeElement;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.BeanParamReader;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PathSegments;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.http.generator.core.UType;
import io.avaje.http.generator.core.Util;
import io.avaje.http.generator.core.WebMethod;

/** Write code to register Web route for a given controller method. */
class ClientMethodWriter {

  private static final KnownResponse KNOWN_RESPONSE = new KnownResponse();
  private static final String BODY_HANDLER = "java.net.http.HttpResponse.BodyHandler";
  private static final String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";
  private static final String HTTP_CALL = "io.avaje.http.client.HttpCall";

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;
  private final ProcessingContext ctx;
  private final UType returnType;
  private MethodParam bodyHandlerParam;
  private String methodGenericParams = "";

  ClientMethodWriter(MethodReader method, Append writer, ProcessingContext ctx) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.ctx = ctx;
    this.returnType = Util.parseType(method.returnType());
  }

  void addImportTypes(ControllerReader reader) {
    reader.addImportTypes(returnType.importTypes());
    for (final MethodParam param : method.params()) {
      final var type = param.utype();
      final var type0 = type.param0();
      final var type1 = type.param1();
      reader.addImportType(type.mainType().replace("[]", ""));
      if (type0 != null) reader.addImportType(type0.replace("[]", ""));
      if (type1 != null) reader.addImportType(type1.replace("[]", ""));
    }
  }

  private void methodStart(Append writer) {
    for (final MethodParam param : method.params()) {
      checkBodyHandler(param);
    }
    writer.append("  // %s %s", webMethod, method.webMethodPath()).eol();
    writer.append("  @Override").eol();
    writer.append(
        "  public %s%s %s(", methodGenericParams, returnType.shortType(), method.simpleName());
    var count = 0;
    for (final MethodParam param : method.params()) {
      if (count++ > 0) {
        writer.append(", ");
      }
      writer.append(param.utype().shortType()).append(" ");
      writer.append(param.name());
    }
    writer.append(") {").eol();
  }

  /** Assign a method parameter as *the* BodyHandler. */
  private void checkBodyHandler(MethodParam param) {
    if (param.rawType().startsWith(BODY_HANDLER)) {
      param.setResponseHandler();
      bodyHandlerParam = param;
      methodGenericParams = param.utype().genericParams();
    }
  }

  void write() {
    methodStart(writer);
    writer.append("    ");
    if (!method.isVoid()) {
      writer.append("return ");
    }
    writer.append("clientContext.request()").eol();

    final var pathSegments = method.pathSegments();
    final var segments = pathSegments.segments();

    writeHeaders();
    writePaths(segments);
    writeQueryParams(pathSegments);
    writeBeanParams(pathSegments);
    writeFormParams(pathSegments);
    writeBody();
    writeEnd();
  }

  private void writeEnd() {
    final var webMethod = method.webMethod();
    writer.append("      .%s()", webMethod.name()).eol();
    if (returnType == UType.VOID) {
      writer.append("      .asVoid();").eol();
    } else {
      final var known = KNOWN_RESPONSE.get(returnType.full());
      if (known != null) {
        writer.append("      %s", known).eol();
      }else if (COMPLETABLE_FUTURE.equals(returnType.mainType())) {
    writeAsyncResponse();
  } else if (HTTP_CALL.equals(returnType.mainType())) {
    writeCallResponse();
  } else {
    writeSyncResponse();
  }
    }
    writer.append("  }").eol().eol();
  }

  private void writeSyncResponse() {
    writer.append("      ");
    final var type0 = returnType.mainType();
    final var type1 = returnType.param0();
    writeResponse(type0, type1);
  }

  private void writeAsyncResponse() {
    writer.append("      .async()");
    final var type0 = returnType.param0();
    final var type1 = returnType.param1();
    writeResponse(type0, type1);
  }

  private void writeCallResponse() {
    writer.append("      .call()");
    final var type0 = returnType.param0();
    final var type1 = returnType.param1();
    writeResponse(type0, type1);
  }

  private void writeResponse(String type0, String type1) {
    if (isList(type0)) {
      writer.append(".list(%s.class);", Util.shortName(type1)).eol();
    } else if (isStream(type0)) {
      writer.append(".stream(%s.class);", Util.shortName(type1)).eol();
    } else if (isHttpResponse(type0)) {
      writeWithHandler();
    } else {
      writer.append(".bean(%s.class);", Util.shortName(type0)).eol();
    }
  }

  private void writeWithHandler() {
    if (bodyHandlerParam != null) {
      writer.append(".handler(%s);", bodyHandlerParam.name()).eol();
    } else {
      writer.append(".handler(responseHandler);").eol(); // Better to barf here?
    }
  }

  private void writeQueryParams(PathSegments pathSegments) {
    for (final MethodParam param : method.params()) {
      final var paramType = param.paramType();
      if ((paramType == ParamType.QUERYPARAM) && (pathSegments.segment(param.paramName()) == null)) {
    if (isMap(param)) {
      writer.append("      .queryParam(%s)", param.name()).eol();
    } else {
      writer.append("      .queryParam(\"%s\", %s)", param.paramName(), param.name()).eol();
    }
  }
    }
  }

  private void writeHeaders() {
    for (final MethodParam param : method.params()) {
      final var paramType = param.paramType();
      if (paramType == ParamType.HEADER) {
        if (isMap(param)) {
          writer.append("      .header(%s)", param.name()).eol();
        } else {
          writer.append("      .header(\"%s\", %s)", param.paramName(), param.name()).eol();
        }
      }
    }
  }

  private void writeBeanParams(PathSegments segments) {
    for (final MethodParam param : method.params()) {
      final var varName = param.name();
      final var paramType = param.paramType();
      final var segment = segments.segment(varName);
      if (segment == null && paramType == ParamType.BEANPARAM) {
        final var formBeanType = ctx.typeElement(param.rawType());
        final var form =
            new BeanParamReader(
                ctx, formBeanType, param.name(), param.shortType(), ParamType.QUERYPARAM);
        form.writeFormParams(writer);
      }
    }
  }

  private void writeFormParams(PathSegments segments) {
    for (final MethodParam param : method.params()) {
      final var varName = param.name();
      final var paramType = param.paramType();
      final var segment = segments.segment(varName);
      if (segment == null) {
        // not a path or matrix parameter
        writeFormParam(param, paramType);
      }
    }
  }

  private void writeFormParam(MethodParam param, ParamType paramType) {
    if (paramType == ParamType.FORMPARAM) {
      if (isMap(param)) {
        writer.append("      .formParam(%s)", param.name()).eol();
      } else {
        writer.append("      .formParam(\"%s\", %s)", param.paramName(), param.name()).eol();
      }
    } else if (paramType == ParamType.FORM) {
      final var formBeanType = ctx.typeElement(param.rawType());
      final var form =
          new BeanParamReader(
              ctx, formBeanType, param.name(), param.shortType(), ParamType.FORMPARAM);
      form.writeFormParams(writer);
    }
  }

  private void writeBody() {
    for (final MethodParam param : method.params()) {
      final var paramType = param.paramType();
      if (paramType == ParamType.BODY) {
        writer.append("      .body(%s)", param.name()).eol();
      }
    }
  }

  private void writePaths(Set<PathSegments.Segment> segments) {
    if (!segments.isEmpty()) {
      writer.append("      ");
    }
    for (final PathSegments.Segment segment : segments) {
      if (segment.isLiteral()) {
        writer.append(".path(\"").append(segment.literalSection()).append("\")");
      } else {
        writer.append(".path(").append(segment.name()).append(")");
        // TODO: matrix params
      }
    }
    if (!segments.isEmpty()) {
      writer.eol();
    }
  }

  private boolean isMap(MethodParam param) {
    return isMap(param.utype().mainType());
  }

  private boolean isMap(String type0) {
    return "java.util.Map".equals(type0);
  }

  private boolean isList(String type0) {
    return "java.util.List".equals(type0);
  }

  private boolean isStream(String type0) {
    return "java.util.stream.Stream".equals(type0);
  }

  private boolean isHttpResponse(String type0) {
    return "java.net.http.HttpResponse".equals(type0);
  }
}
