package io.avaje.http.generator.jex;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;

import java.util.List;

class JexAdapter implements PlatformAdapter {

  static final String JEX_CONTEXT = "io.avaje.jex.Context";

  @Override
  public boolean isContextType(String rawType) {
    return JEX_CONTEXT.equals(rawType);
  }

  @Override
  public String platformVariable(String rawType) {
    return "ctx";
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
    for (String role : roles) {
      controller.addStaticImportType(role);
    }
  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName) {
    writer.append("ctx.%s(\"%s\")", paramType, paramName);
  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName, String paramDefault) {
    writer.append("withDefault(ctx.%s(\"%s\"), \"%s\")", paramType, paramName, paramDefault);
  }
}
