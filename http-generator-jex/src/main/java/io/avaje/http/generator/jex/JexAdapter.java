package io.avaje.http.generator.jex;

import java.util.List;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.UType;

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
  public String bodyAsClass(UType uType) {
    if ("java.lang.String".equals(uType.full())) {
      return "ctx.body()";
    }
    return "ctx.bodyAsClass(" + uType.mainType() + ".class)";
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
  public void writeReadParameter(Append writer, ParamType paramType, String paramName, String paramDefault) {
    writer.append("withDefault(ctx.%s(\"%s\"), \"%s\")", paramType, paramName, paramDefault);
  }

  @Override
  public void writeReadCollectionParameter(Append writer, ParamType paramType, String paramName) {
    if (paramType != ParamType.QUERYPARAM) {
      throw new UnsupportedOperationException(
          "Only MultiValue Query Params are supported in Jex");
    }
    writer.append("ctx.queryParams(\"%s\")", paramName);
  }

  @Override
  public void writeReadCollectionParameter(
      Append writer, ParamType paramType, String paramName, List<String> paramDefault) {
    if (paramType != ParamType.QUERYPARAM) {
      throw new UnsupportedOperationException(
          "Only MultiValue Query Params are supported in Jex");
    }
    writer.append("withDefault(ctx.queryParams(\"%s\"), java.util.List.of(\"%s\"))", paramName, String.join(",", paramDefault));
  }
}
