package io.avaje.http.generator.spark;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;

import java.util.List;

class SparkAdapter implements PlatformAdapter {

  static final String REQ = "spark.Request";
  static final String RES = "spark.Response";

  @Override
  public boolean isContextType(String rawType) {
    return REQ.equals(rawType) || RES.equals(rawType);
  }

  @Override
  public String platformVariable(String rawType) {
    if (REQ.equals(rawType)) {
      return "request";
    }
    if (RES.equals(rawType)) {
      return "response";
    }
    return "unknownVariable for: " + rawType;
  }

  @Override
  public boolean isBodyMethodParam() {
    return false;
  }

  @Override
  public String bodyAsClass(String shortType) {
    return "ctx.bodyAsClass(" + shortType + ".class)";
  }

  @Override
  public String indent() {
    return "    ";
  }

  @Override
  public void controllerRoles(List<String> roles, ControllerReader controller) {
    addRoleImports(roles, controller);
  }

  @Override
  public void methodRoles(List<String> roles, ControllerReader controller) {
    addRoleImports(roles, controller);
  }

  private void addRoleImports(List<String> roles, ControllerReader controller) {

  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName) {
    switch (paramType) {
      case PATHPARAM:
        writer.append("request.params(\"%s\")", paramName);
        break;
      case QUERYPARAM:
      case FORMPARAM:
        writer.append("request.queryParams(\"%s\")", paramName);
        break;
      default:
        writer.append("request.%s(\"%s\")", paramType, paramName);
    }
  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName, String paramDefault) {
    switch (paramType) {
      case PATHPARAM:
        writer.append("request.params(\"%s\")", paramName);
        break;
      case QUERYPARAM:
      case FORMPARAM:
        writer.append("request.queryParamsOrDefault(\"%s\",\"%s\")", paramName, paramDefault);
        break;
      default:
        writer.append("request.%s(\"%s\",\"%s\")", paramType, paramName, paramDefault);
    }
  }
}
