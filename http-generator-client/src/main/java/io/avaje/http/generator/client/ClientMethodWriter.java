package io.avaje.http.generator.client;

import static io.avaje.http.generator.core.ProcessingContext.*;
import io.avaje.http.generator.core.*;
import io.avaje.http.generator.core.PathSegments.Segment;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import static java.util.stream.Collectors.toMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
  private final boolean useConfig;
  private final Map<String, String> segmentPropertyMap;

  ClientMethodWriter(MethodReader method, Append writer, boolean useJsonb) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.webMethod();
    this.returnType = Util.parseType(method.returnType());
    this.useJsonb = useJsonb;
    this.timeout = method.timeout();
    this.useConfig = ProcessingContext.typeElement("io.avaje.config.Config") != null;

    this.segmentPropertyMap = method.pathSegments().segments().stream()
      .filter(Segment::isProperty)
      .collect(toMap(Segment::name, s -> Util.sanitizeName(s.name()).toUpperCase()));
  }

  void addImportTypes(ControllerReader reader) {
    reader.addImportTypes(returnType.importTypes());
    method.throwsList().stream()
      .map(UType::parse)
      .map(UType::importTypes)
      .forEach(reader::addImportTypes);

    for (final MethodParam param : method.params()) {
      reader.addImportTypes(param.utype().importTypes());
    }

    if (useConfig && !segmentPropertyMap.isEmpty()) {
      reader.addImportType("io.avaje.config.Config");
    }
  }

  private void methodStart(Append writer) {
    for (MethodParam param : method.params()) {
      checkBodyHandler(param);
    }
    method.checkArgumentNames();

    segmentPropertyMap.forEach((k, v) -> {
      writer.append("  private static final String %s = ", v);
      final String getProperty = useConfig ? "Config.get(" : "System.getProperty(";
      writer.append(getProperty).append("\"%s\");", k).eol();
    });

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

      if (param.overrideVarNameError()) {
        writer.append("/** !Error! */ ");
      }
      final var finalType = isVarArg ? paramType.substring(0, paramType.length() - 2) + "..." : paramType;
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
    writeErrorMapper();
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
      final var params = type.importTypes().stream()
        .skip(1)
        .map(Util::shortName)
        .collect(Collectors.joining(".class, "));

      writer.append("Types.newParameterizedType(%s.class, %s.class)", Util.shortName(type.mainType()), params);
    } else {
      writer.append("%s.class", Util.shortName(type.mainType()));
    }
  }

  private void writeQueryParams(PathSegments pathSegments) {
    boolean clientImportError = false;
    for (final MethodParam param : method.params()) {
      final ParamType paramType = param.paramType();
      if (paramType == ParamType.QUERYPARAM && pathSegments.segment(param.paramName()) == null) {
        if (isMap(param)) {
          writer.append("      .queryParam(%s)", param.name());
        } else {
          writer.append("      .queryParam(\"%s\", %s)", param.paramName(), param.name());
        }
        if (param.overrideVarNameError()) {
          clientImportError = true;
          writer.append(" // !Error! with %s", param.name());
        }
        writer.eol();
      }
    }
    if (clientImportError) {
      writer.append("   ; !Error!").eol();
      writer.eol();
      writer.append("   // Explicit @QueryParam(\"...\") required with @Client.Import").eol();
      writer.append("   // Refer to: https://avaje.io/http/client/import#error").eol();
      writer.eol();
      logError(
        "Explicit @QueryParam/@Header annotations required when using @Client.Import on an interface that has already been compiled.",
        method.element());
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

  private void writeErrorMapper() {
    method.throwsList().stream()
      .map(ProcessingContext::asElement)
      .filter(
        e ->
          isAssignable2Interface(
            e.getQualifiedName().toString(), "java.lang.RuntimeException"))
      .filter(
        e ->
          ElementFilter.constructorsIn(e.getEnclosedElements()).stream()
            .filter(c -> c.getParameters().size() == 1)
            .map(c -> c.getParameters().get(0).asType().toString())
            .map(Util::trimAnnotations)
            .anyMatch("io.avaje.http.client.HttpException"::equals))
      .findFirst()
      .map(TypeElement::getQualifiedName)
      .map(Object::toString)
      .map(Util::shortName)
      .ifPresent(
        exception ->
          writer
            .append("      .errorMapper(%s::new)", exception)
            .eol());
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
    boolean first = true;
    Iterator<Segment> iterator = segments.iterator();
    boolean sentinel = true;
    boolean noSlash = false;
    var size = segments.size();
    while (sentinel) {
      PathSegments.Segment segment = iterator.hasNext() ? iterator.next() : null;
      if (segment == null) {
        sentinel = false;
        if (size != 0) {
          writer.append("\")");
        }
        continue;
      }
      if (first) {
        writer.append(".path(\"");
        first = false;
      }
      if (noSlash) {
        writer.append("/");
      }
      noSlash = true;
      if (segment.isLiteral()) {
        writer.append(segment.literalSection());
      } else if (segment.isProperty()) {

        writer.append("\" + %s + \"", segmentPropertyMap.get(segment.name()));

      } else {
        writer.append(segment.name());
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
