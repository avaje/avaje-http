package io.avaje.http.generator.vertx;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.UType;
import java.util.List;

final class VertxAdapter implements PlatformAdapter {

  private static final String ROUTING_CONTEXT = "io.vertx.ext.web.RoutingContext";
  private static final String JSON_OBJECT = "io.vertx.core.json.JsonObject";
  private static final String JSON_ARRAY = "io.vertx.core.json.JsonArray";

  @Override
  public boolean isContextType(String rawType) {
    return ROUTING_CONTEXT.equals(rawType);
  }

  @Override
  public String platformVariable(String rawType) {
    return "ctx";
  }

  @Override
  public String bodyAsClass(UType type) {
    final String fullType = type.full();
    if ("java.lang.String".equals(fullType)) {
      return "ctx.body().asString()";
    }
    if ("byte[]".equals(fullType)) {
      return "ctx.body().buffer().getBytes()";
    }
    if ("io.vertx.core.buffer.Buffer".equals(fullType)) {
      return "ctx.body().buffer()";
    }
    if (JSON_OBJECT.equals(fullType)) {
      return "ctx.body().asJsonObject()";
    }
    if (JSON_ARRAY.equals(fullType)) {
      return "ctx.body().asJsonArray()";
    }
    return "ctx.body().asPojo(" + type.mainType() + ".class)";
  }

  @Override
  public boolean isBodyMethodParam() {
    return false;
  }

  @Override
  public String indent() {
    return "    ";
  }

  @Override
  public void controllerRoles(List<String> roles, ControllerReader controller) {
  }

  @Override
  public void methodRoles(List<String> roles, ControllerReader controller) {
  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName) {
    switch (paramType) {
      case PATHPARAM:
        writer.append("ctx.pathParam(\"%s\")", paramName);
        break;
      case QUERYPARAM:
      case FORMPARAM:
        writer.append("ctx.request().getParam(\"%s\")", paramName);
        break;
      case HEADER:
        writer.append("ctx.request().getHeader(\"%s\")", paramName);
        break;
      case COOKIE:
        writer.append("cookieValue(ctx, \"%s\")", paramName);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported parameter type for Vert.x: " + paramType);
    }
  }

  @Override
  public void writeReadParameter(
      Append writer,
      ParamType paramType,
      String paramName,
      String paramDefault
  ) {
    writer.append("withDefault(");
    writeReadParameter(writer, paramType, paramName);
    writer.append(", \"%s\")", escapeJava(paramDefault));
  }

  @Override
  public void writeReadMapParameter(Append writer, ParamType paramType) {
    switch (paramType) {
      case QUERYPARAM:
      case FORMPARAM:
      case FORM:
        writer.append(
        "ctx.request().params().entries().stream()"
          + ".collect(java.util.stream.Collectors.groupingBy("
          + "java.util.Map.Entry::getKey, "
          + "java.util.stream.Collectors.mapping("
          + "java.util.Map.Entry::getValue, java.util.stream.Collectors.toList())))"
        );
        break;
      default:
        throw new UnsupportedOperationException("Only query/form map parameters are supported in Vert.x");
    }
  }

  @Override
  public void writeReadCollectionParameter(Append writer, ParamType paramType, String paramName) {
    switch (paramType) {
      case QUERYPARAM:
        writer.append("ctx.queryParam(\"%s\")", paramName);
        break;
      case FORMPARAM:
      case FORM:
        writer.append("ctx.request().params().getAll(\"%s\")", paramName);
        break;
      default:
        throw new UnsupportedOperationException("Only query/form multi-value parameters are supported in Vert.x");
    }
  }

  @Override
  public void writeReadCollectionParameter(
      Append writer,
      ParamType paramType,
      String paramName,
      List<String> paramDefault
  ) {
    writer.append("withDefault(");
    writeReadCollectionParameter(writer, paramType, paramName);
    writer.append(", java.util.List.of(");
    for (int i = 0; i < paramDefault.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      writer.append("\"%s\"", escapeJava(paramDefault.get(i)));
    }
    writer.append("))");
  }

  @Override
  public void writeAcceptLanguage(Append writer) {
    writer.append("ctx.request().getHeader(\"%s\")", Constants.ACCEPT_LANGUAGE);
  }

  private String escapeJava(String value) {
    if (value == null) {
      return "";
    }
    return value
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t");
  }
}
