package io.avaje.http.generator.client;

import static io.avaje.http.generator.core.ProcessingContext.*;
import io.avaje.http.generator.core.*;

import javax.lang.model.element.TypeElement;

import java.util.List;
import java.util.Optional;
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
  private final UType returnType;
  private MethodParam bodyHandlerParam;
  private String methodGenericParams = "";
  private final boolean useJsonb;
  private final Optional<RequestTimeoutPrism> timeout;

  ClientMethodWriter(MethodReader method, Append writer, boolean useJsonb) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.returnType = Util.parseType(method.returnType());
    this.useJsonb = useJsonb;
    this.timeout = method.timeout();
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
    AnnotationUtil.writeAnnotations(writer, method.element());
    writer.append("  public %s%s %s(", methodGenericParams, returnType.shortType(), method.simpleName());
    int count = 0;
    List<MethodParam> params = method.params();
    for (int i = 0; i < params.size(); i++) {
      MethodParam param = params.get(i);
      if (count++ > 0) {
        writer.append(", ");
      }
      final var isVarArg = Util.isVarArg(param.element(), i);

      final var paramType =
          "java.util.function.Supplier<?extendsjava.io.InputStream>".equals(param.utype().full())
              ? "Supplier<? extends InputStream>"
              : param.utype().shortType();

      final var finalType =
          isVarArg ? paramType.substring(0, paramType.length() - 2) + "..." : paramType;
      writer.append(finalType).append(" ");
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
    writer.append("client.request()").eol();

    PathSegments pathSegments = method.pathSegments();
    Set<PathSegments.Segment> segments = pathSegments.segments();

    writeHeaders();
    writePaths(segments);
    writeQueryParams(pathSegments);
    writeBeanParams(pathSegments);
    writeFormParams(pathSegments);
    timeout.ifPresent(this::writeTimeout);
    writeBody();
    writeEnd();
  }

  private void writeTimeout(RequestTimeoutPrism p) {

    writer.append("      .requestTimeout(of(%s, %s))", p.value(), p.chronoUnit()).eol();
  }

private void writeEnd() {
    final var webMethod = method.webMethod();
    writer.append("      .%s()", webMethod.name()).eol();
    if (returnType == UType.VOID) {
      writer.append("      .asVoid();").eol();
    } else {
      String known = KNOWN_RESPONSE.get(returnType.full());
      if (known != null) {
        writer.append("      %s", known).eol();
      } else if (COMPLETABLE_FUTURE.equals(returnType.mainType())) {
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
      writer.append(");").eol();
    } else if (isStream(mainType)) {
      writer.append(".stream(");
      writeGeneric(param1);
      writer.append(");").eol();
    } else if (isHttpResponse(mainType)) {
      if (bodyHandlerParam == null) {
        final UType paramType = type.paramRaw();
        if ("java.util.List".equals(paramType.mainType())) {
          writer.append(".asList(");
          writeGeneric(paramType.paramRaw());
        } else if ("java.util.stream.Stream".equals(paramType.mainType())) {
          writer.append(".asStream(");
          writeGeneric(paramType.paramRaw());
        } else {
          writer.append(".as(");
          writeGeneric(paramType);
        }
        writer.append(");").eol();
      } else {
        writer.append(".handler(%s);", bodyHandlerParam.name()).eol();
      }
    } else {
      writer.append(".bean(");
      writeGeneric(type);
      writer.append(");").eol();
    }
  }

  void writeGeneric(UType type) {
    if (useJsonb && type.isGeneric()) {
      final var params =
          type.importTypes().stream()
                  .skip(1)
                  .map(Util::shortName)
                  .collect(Collectors.joining(".class, "));

      writer.append("Types.newParameterizedType(%s.class, %s.class)", Util.shortName(type.mainType()), params);
    } else {
      writer.append("%s.class", Util.shortName(type.mainType()));
    }
  }

  private void writeQueryParams(PathSegments pathSegments) {
    for (final MethodParam param : method.params()) {
      final ParamType paramType = param.paramType();
      if (paramType == ParamType.QUERYPARAM && pathSegments.segment(param.paramName()) == null) {
        if (isMap(param)) {
          writer.append("      .queryParam(%s)", param.name()).eol();
        } else {
          writer.append("      .queryParam(\"%s\", %s)", param.paramName(), param.name()).eol();
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
        TypeElement formBeanType = typeElement(param.rawType());
        BeanParamReader form = new BeanParamReader(formBeanType, param.name(), param.shortType(), ParamType.QUERYPARAM);
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
      TypeElement formBeanType = typeElement(param.rawType());
      BeanParamReader form = new BeanParamReader(formBeanType, param.name(), param.shortType(), ParamType.FORMPARAM);
      form.writeFormParams(writer);
    }
  }

  private void writeBody() {
    for (MethodParam param : method.params()) {
      if (param.paramType() == ParamType.BODY) {
        var type = param.utype().full();
        if (directBodyType(type)) {
          writer.append("      .body(%s)", param.name()).eol();
        } else {
          writer.append("      .body(%s, ", param.name());
          writeGeneric(param.utype());
          writer.append(")").eol();
        }
        return;
      }
    }
  }

  /**
   * Return true for body types that are directly supported.
   */
  private static boolean directBodyType(String type) {
    return "java.net.http.HttpRequest.BodyPublisher".equals(type)
      || "java.lang.String".equals(type)
      || "byte[]".equals(type)
      || "java.io.InputStream".equals(type)
      || "java.util.function.Supplier<?extendsjava.io.InputStream>".equals(type)
      || "java.util.function.Supplier<java.io.InputStream>".equals(type)
      || "java.nio.file.Path".equals(type)
      || "io.avaje.http.client.BodyContent".equals(type);
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
