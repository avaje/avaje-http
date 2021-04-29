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

  private final MethodReader method;
  private final Append writer;
  private final WebMethod webMethod;
  private final ProcessingContext ctx;
  private final UType returnType;

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
    }
    writer.append(") {").eol();
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
    writer.append("      .%s()", webMethod.name().toLowerCase()).eol();
    if (returnType == UType.VOID) {
      writer.append("      .asDiscarding();").eol();
    } else {
      String known = KNOWN_RESPONSE.get(returnType.full());
      if (known != null) {
        writer.append("      %s", known).eol();
      } else if (isReturnList()) {
        writer.append("      .list(%s.class);", Util.shortName(returnType.param0())).eol();
      } else {
        writer.append("      .bean(%s.class);", Util.shortName(returnType.full())).eol();
      }
    }
    writer.append("  }").eol().eol();
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

  private boolean isReturnList() {
    return returnType.shortType().startsWith("List<");
  }

}
