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
      case HEADER:
        writer.append("req.headers().value(\"%s\")", paramName);
        break;

      default:
        writer.append("req.%s().param(\"%s\")", paramType.getType(), paramName);

    }
  }
}
