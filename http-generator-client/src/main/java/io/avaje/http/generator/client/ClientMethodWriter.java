package io.avaje.http.generator.client;

import io.avaje.http.generator.core.*;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

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

  ClientMethodWriter(MethodReader method, Append writer, ProcessingContext ctx) {
    this.method = method;
    this.writer = writer;
    this.webMethod = method.getWebMethod();
    this.ctx = ctx;
    this.returnType = Util.parseType(method.getReturnType());
  }

  void addImportTypes(ControllerReader reader) {
    reader.addImportTypes(returnType.importTypes());
    for (MethodParam param : method.getParams()) {
      param.addImports(reader);
    }
  }

  private void methodStart(Append writer) {
    writer.append("  // %s %s", webMethod, method.getWebMethodPath()).eol();
    writer.append("  @Override").eol();
    writer.append("  public %s %s(", returnType.shortType(), method.simpleName());
    int count = 0;
    for (MethodParam param : method.getParams()) {
      if (count++ > 0) {
        writer.append(", ");
      }
      writer.append(param.getShortType()).append(" ");
      writer.append(param.getName());
      checkBodyHandler(param);
    }
    writer.append(") {").eol();
  }

  /**
   * Assign a method parameter as *the* BodyHandler.
   */
  private void checkBodyHandler(MethodParam param) {
    if (param.getRawType().startsWith(BODY_HANDLER)) {
      bodyHandlerParam = param;
    }
  }

  void write() {
    methodStart(writer);
    writer.append("    ");
    if (!method.isVoid()) {
      writer.append("return ");
    }
    writer.append("clientContext.request()").eol();

    PathSegments pathSegments = method.getPathSegments();
    Set<PathSegments.Segment> segments = pathSegments.getSegments();

    writeHeaders();
    writePaths(segments);
    writeQueryParams(pathSegments);
    writeFormParams();
    writeBody();

    WebMethod webMethod = method.getWebMethod();
    writer.append("      .%s()", webMethod.name()).eol();
    if (returnType == UType.VOID) {
      writer.append("      .asDiscarding();").eol();
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
    String type0 = returnType.mainType();
    String type1 = returnType.param0();
    writeResponse(type0, type1);
  }

  private void writeAsyncResponse() {
    writer.append("      .async()");
    String type0 = returnType.param0();
    String type1 = returnType.param1();
    writeResponse(type0, type1);
  }

  private void writeCallResponse() {
    writer.append("      .call()");
    String type0 = returnType.param0();
    String type1 = returnType.param1();
    writeResponse(type0, type1);
  }

  private void writeResponse(String type0, String type1) {
    if (isList(type0)) {
      writer.append(".list(%s.class);", Util.shortName(type1)).eol();
    } else if (isStream(type0)) {
      writer.append(".stream(%s.class);", Util.shortName(type1)).eol();
    } else if (isHttpResponse(type0)){
      writeWithHandler();
    } else {
      writer.append(".bean(%s.class);", Util.shortName(type0)).eol();
    }
  }

  private void writeWithHandler() {
    if (bodyHandlerParam != null) {
      writer.append(".withHandler(%s);", bodyHandlerParam.getName()).eol();
    } else {
      writer.append(".withHandler(responseHandler);").eol(); // Better to barf here?
    }
  }

  private void writeQueryParams(PathSegments pathSegments) {
    List<MethodParam> params = method.getParams();
    for (MethodParam param : params) {
      ParamType paramType = param.getParamType();
      if (paramType == ParamType.QUERYPARAM) {
        PathSegments.Segment segment = pathSegments.segment(param.getParamName());
        if (segment == null) {
          writer.append("      .queryParam(\"%s\", %s)", param.getParamName(), param.getName()).eol();
        }
      }
    }
  }

  private void writeHeaders() {
    for (MethodParam param : method.getParams()) {
      ParamType paramType = param.getParamType();
      if (paramType == ParamType.HEADER) {
        writer.append("      .header(\"%s\", %s)", param.getParamName(), param.getName()).eol();
      }
    }
  }

  private void writeFormParams() {
    for (MethodParam param : method.getParams()) {
      ParamType paramType = param.getParamType();
      if (paramType == ParamType.FORMPARAM) {
        writer.append("      .formParam(\"%s\", %s)", param.getParamName(), param.getName()).eol();
      } else if (paramType == ParamType.FORM) {
        TypeElement formBeanType = ctx.getTypeElement(param.getRawType());
        BeanParamReader form = new BeanParamReader(ctx, formBeanType, param.getName(), param.getShortType(), ParamType.FORMPARAM);
        form.writeFormParams(writer);
      }
    }
  }

  private void writeBody() {
    for (MethodParam param : method.getParams()) {
      ParamType paramType = param.getParamType();
      if (paramType == ParamType.BODY) {
        writer.append("      .body(%s)", param.getName()).eol();
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
