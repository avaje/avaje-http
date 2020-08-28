package io.avaje.http.generator.helidon;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;

import java.util.List;

class HelidonPlatformAdapter implements PlatformAdapter {

  static final String HELIDON_REQ = "io.helidon.webserver.ServerRequest";
  static final String HELIDON_RES = "io.helidon.webserver.ServerResponse";
  static final String HELIDON_FORMPARAMS = "io.helidon.common.http.FormParams";

  @Override
  public boolean isContextType(String rawType) {
    return HELIDON_REQ.equals(rawType) || HELIDON_RES.equals(rawType) || HELIDON_FORMPARAMS.equals(rawType);
  }

  @Override
  public String platformVariable(String rawType) {
    if (HELIDON_REQ.equals(rawType)) {
      return "req";
    }
    if (HELIDON_RES.equals(rawType)) {
      return "res";
    }
    if (HELIDON_FORMPARAMS.equals(rawType)) {
      return "formParams";
    }
    return "unknownVariable for: "+rawType;
  }

  @Override
  public boolean isBodyMethodParam() {
    return true;
  }

  @Override
  public String bodyAsClass(String shortType) {
    return "body";
  }

  @Override
  public String indent() {
    return "  ";
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
    // nothing here yet
  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName) {
    switch (paramType) {
      case PATHPARAM:
        writer.append("req.path().param(\"%s\")", paramName);
        break;
      case QUERYPARAM:
        writer.append("req.queryParams().first(\"%s\").orElse(null)", paramName);
        break;
      case FORMPARAM:
        writer.append("formParams.first(\"%s\").orElse(null)", paramName);
        break;
      case HEADER:
        writer.append("req.headers().value(\"%s\").orElse(null)", paramName);
        break;
      case COOKIE:
        writer.append("req.headers().cookies().first(\"%s\").orElse(null)", paramName);
        break;
      case BODY:
      case BEANPARAM:
      case FORM:
      default:
        writer.append("null // TODO req.%s().param(\"%s\")", paramType.getType(), paramName);
    }
  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName, String paramDefault) {
    switch (paramType) {
      case PATHPARAM:
        writer.append("req.path().param(\"%s\")", paramName);
        break;
      case QUERYPARAM:
        writer.append("req.queryParams().first(\"%s\").orElse(\"%s\")", paramName, paramDefault);
        break;
      case FORMPARAM:
        writer.append("formParams.first(\"%s\").orElse(\"%s\")", paramName, paramDefault);
        break;
      case HEADER:
        writer.append("req.headers().value(\"%s\").orElse(\"%s\")", paramName, paramDefault);
        break;
      case COOKIE:
        writer.append("req.headers().cookies().first(\"%s\").orElse(\"%s\")", paramName, paramDefault);
        break;
      case BODY:
      case BEANPARAM:
      case FORM:
      default:
        writer.append("null // TODO req.%s().param(\"%s\")", paramType.getType(), paramName);
    }
  }
}
