package io.dinject.webroutegen;

import java.util.List;

class HelidonPlatformAdapter implements PlatformAdapter {

  static final String JAVALIN3_CONTEXT = "io.javalin.http.Context";
  static final String JAVALIN3_ROLES = "io.javalin.core.security.SecurityUtil.roles";

  @Override
  public boolean isContextType(String rawType) {
    return JAVALIN3_CONTEXT.equals(rawType);
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
    controller.addStaticImportType(JAVALIN3_ROLES);
    for (String role : roles) {
      controller.addStaticImportType(role);
    }
  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName) {
    switch (paramType) {
      case PATHPARAM:
        writer.append("req.path().param(\"%s\")", paramName);
        break;
      case QUERYPARAM:
      case FORMPARAM:
        writer.append("req.queryParams().first(\"%s\").orElse(null)", paramName);
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
      case FORMPARAM:
        writer.append("req.queryParams().first(\"%s\").orElse(\"%s\")", paramName, paramDefault);
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
