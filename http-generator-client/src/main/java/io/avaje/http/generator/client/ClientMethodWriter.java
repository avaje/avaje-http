package io.avaje.http.generator.client;

import io.avaje.http.generator.core.*;

import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Write code to register Web route for a given controller method.
 */
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
  private final boolean useJsonb;

  ClientMethodWriter(MethodReader method, Append writer, ProcessingContext ctx, boolean useJsonb) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.ctx = ctx;
    this.returnType = Util.parseType(method.returnType());
    this.useJsonb = useJsonb;
  }

  void addImportTypes(ControllerReader reader) {
    reader.addImportTypes(returnType.importTypes());
    for (final MethodParam param : method.params()) {
      reader.addImportTypes(param.utype().importTypes());
    }
  }

  private void methodStart(Append writer) {
    for (MethodParam param : method.params()) {
      checkBodyHandler(param);
    }
    writer.append("  // %s %s", webMethod, method.webMethodPath()).eol();
    writer.append("  @Override").eol();
    writer.append("  public %s%s %s(", methodGenericParams, returnType.shortType(), method.simpleName());
    int count = 0;
    for (MethodParam param : method.params()) {
      if (count++ > 0) {
        writer.append(", ");
      }
      writer.append(param.utype().shortType()).append(" ");
      writer.append(param.name());
    }
    writer.append(") {").eol();
  }

  /**
   * Assign a method parameter as *the* BodyHandler.
   */
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

    PathSegments pathSegments = method.pathSegments();
    Set<PathSegments.Segment> segments = pathSegments.segments();

    writeHeaders();
    writePaths(segments);
    writeQueryParams(pathSegments);
    writeBeanParams(pathSegments);
    writeFormParams(pathSegments);
    writeBody();
    writeEnd();
  }

  private void writeEnd() {
    WebMethod webMethod = method.webMethod();
    writer.append("      .%s()", webMethod.name()).eol();
    if (returnType == UType.VOID) {
      writer.append("      .asVoid();").eol();
    } else {
      String known = KNOWN_RESPONSE.get(returnType.full());
      if (known != null) {
        writer.append("      %s", known).eol();
      } else {
        if (COMPLETABLE_FUTURE.equals(returnType.mainType())) {
          writeAsyncResponse();
        } else if (HTTP_CALL.equals(returnType.mainType())) {
            writeCallResponse();
        } else {
          writeSyncResponse();
        }
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeSyncResponse() {
    writer.append("      ");
    writeResponse(returnType);
  }

  private void writeAsyncResponse() {
    writer.append("      .async()");
    writeResponse(returnType.paramRaw());
  }

  private void writeCallResponse() {
    writer.append("      .call()");
    writeResponse(returnType.paramRaw());
  }

  private void writeResponse(UType type) {
    final var mainType = type.mainType();
    final var param1 = type.paramRaw();
    if (isList(mainType)) {
      writer.append(".list(");
      writeGeneric(param1);
    } else if (isStream(mainType)) {
      writer.append(".stream(");
      writeGeneric(param1);
    } else if (isHttpResponse(mainType)) {
      writeWithHandler();
    } else {
      writer.append(".bean(");
      writeGeneric(type);
    }
  }

  void writeGeneric(UType type) {
    if (useJsonb && type.isGeneric()) {
      final var params =
          type.importTypes().stream()
                  .skip(1)
                  .map(Util::shortName)
                  .collect(Collectors.joining(".class, "));

      writer.append(
          "Types.newParameterizedType(%s.class, %s.class)", Util.shortName(type.mainType()), params);
    } else {
      writer.append("%s.class", Util.shortName(type.mainType()));
    }
    writer.append(");");

    writer.eol();
  }

  private void writeWithHandler() {
    if (bodyHandlerParam != null) {
      writer.append(".handler(%s);", bodyHandlerParam.name()).eol();
    } else {
      writer.append(".handler(responseHandler);").eol(); // Better to barf here?
    }
  }

  private void writeQueryParams(PathSegments pathSegments) {
    for (MethodParam param : method.params()) {
      ParamType paramType = param.paramType();
      if (paramType == ParamType.QUERYPARAM) {
        if (pathSegments.segment(param.paramName()) == null) {
          if (isMap(param)) {
            writer.append("      .queryParam(%s)", param.name()).eol();
          } else {
            writer.append("      .queryParam(\"%s\", %s)", param.paramName(), param.name()).eol();
          }
        }
      }
    }
  }

  private void writeHeaders() {
    for (MethodParam param : method.params()) {
      ParamType paramType = param.paramType();
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
    for (MethodParam param : method.params()) {
      final String varName = param.name();
      ParamType paramType = param.paramType();
      PathSegments.Segment segment = segments.segment(varName);
      if (segment == null && paramType == ParamType.BEANPARAM) {
        TypeElement formBeanType = ctx.typeElement(param.rawType());
        BeanParamReader form = new BeanParamReader(ctx, formBeanType, param.name(), param.shortType(), ParamType.QUERYPARAM);
        form.writeFormParams(writer);
      }
    }
  }

  private void writeFormParams(PathSegments segments) {
    for (MethodParam param : method.params()) {
      final String varName = param.name();
      ParamType paramType = param.paramType();
      PathSegments.Segment segment = segments.segment(varName);
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
      TypeElement formBeanType = ctx.typeElement(param.rawType());
      BeanParamReader form = new BeanParamReader(ctx, formBeanType, param.name(), param.shortType(), ParamType.FORMPARAM);
      form.writeFormParams(writer);
    }
  }

  private void writeBody() {
    for (MethodParam param : method.params()) {
      ParamType paramType = param.paramType();
      if (paramType == ParamType.BODY) {
        writer.append("      .body(%s)", param.name()).eol();
      }
    }
  }

  private void writePaths(Set<PathSegments.Segment> segments) {
    if (!segments.isEmpty()) {
      writer.append("      ");
    }
    for (PathSegments.Segment segment : segments) {
      if (segment.isLiteral()) {
        writer.append(".path(\"").append(segment.literalSection()).append("\")");
      } else {
        writer.append(".path(").append(segment.name()).append(")");
        //TODO: matrix params
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
    return type0.equals("java.util.Map");
  }

  private boolean isList(String type0) {
    return type0.equals("java.util.List");
  }

  private boolean isStream(String type0) {
    return type0.equals("java.util.stream.Stream");
  }

  private boolean isHttpResponse(String type0) {
    return type0.equals("java.net.http.HttpResponse");
  }

}