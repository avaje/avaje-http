package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.*;
import java.util.List;

class JavalinAdapter implements PlatformAdapter {

  static final String JAVALIN3_CONTEXT = "io.javalin.http.Context";

  private final boolean useJsonB;

  JavalinAdapter(boolean useJsonB) {
    this.useJsonB = useJsonB;
  }

  @Override
  public boolean isContextType(String rawType) {
    return JAVALIN3_CONTEXT.equals(rawType);
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
  public String bodyAsClass(UType type) {
    if (useJsonB) {
      return type.shortName() + "JsonType.fromJson(ctx.bodyInputStream())";
    }
    return "ctx.bodyAsClass(" + type.mainType() + ".class)";
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
    for (final String role : roles) {
      controller.addStaticImportType(role);
    }
  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName) {
    writer.append("ctx.%s(\"%s\")", paramType, paramName);
  }

  @Override
  public void writeReadParameter(
      Append writer, ParamType paramType, String paramName, String paramDefault) {
    writer.append("withDefault(ctx.%s(\"%s\"), \"%s\")", paramType, paramName, paramDefault);
  }
}
