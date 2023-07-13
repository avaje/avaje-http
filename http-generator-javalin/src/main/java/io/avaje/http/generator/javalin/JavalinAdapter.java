package io.avaje.http.generator.javalin;

import java.util.List;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.UType;

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
    if ("java.io.InputStream".equals(type.full())) {
      return "ctx.bodyInputStream()";
    } else if ("java.lang.String".equals(type.full())) {
      return "ctx.body()";
    } else if ("byte[]".equals(type.full())) {
      return "ctx.bodyAsBytes()";
    } else {
      if (useJsonB) {
        return type.shortName() + "JsonType.fromJson(ctx.bodyInputStream())";
      }
      return "ctx.<" + type.mainType() + ">bodyStreamAsClass(" + type.mainType() + ".class)";
    }
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

  @Override
  public void writeReadMapParameter(Append writer, ParamType paramType) {
    if (paramType != ParamType.QUERYPARAM) {
      throw new UnsupportedOperationException(
          "Only Query Params have Map<String, List<String>> supported in Javalin");
    }
    writer.append("ctx.queryParamMap()");
  }

  @Override
  public void writeReadCollectionParameter(Append writer, ParamType paramType, String paramName) {
    if (paramType != ParamType.QUERYPARAM) {
      throw new UnsupportedOperationException(
          "Only MultiValue Query Params are supported in Javalin");
    }
    writer.append("ctx.queryParams(\"%s\")", paramName);
  }

  @Override
  public void writeReadCollectionParameter(
      Append writer, ParamType paramType, String paramName, List<String> paramDefault) {
    if (paramType != ParamType.QUERYPARAM) {
      throw new UnsupportedOperationException(
          "Only MultiValue Query Params are supported in Javalin");
    }
    writer.append("withDefault(ctx.queryParams(\"%s\"), java.util.List.of(\"%s\"))", paramName, String.join(",", paramDefault));
  }

  @Override
  public void writeAcceptLanguage(Append writer) {
    writer.append("ctx.header(\"%s\")", Constants.ACCEPT_LANGUAGE);
  }
}
