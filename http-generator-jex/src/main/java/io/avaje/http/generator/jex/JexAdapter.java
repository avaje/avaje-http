package io.avaje.http.generator.jex;

import java.util.List;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.ProcessingContext;
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
  public String bodyAsClass(UType type) {

    if ("java.io.InputStream".equals(type.full())) {
      return "ctx.bodyAsInputStream()";
    } else if ("java.lang.String".equals(type.full())) {
      return "ctx.body()";
    } else if ("byte[]".equals(type.full())) {
      return "ctx.bodyAsBytes()";
    } else if (ProcessingContext.useJsonb()) {
      return type.shortName() + "JsonType.fromJson(ctx.bodyAsInputStream())";
    }
    return "ctx.bodyAsClass(" + type.mainType() + ".class)";
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

    switch (paramType) {
      case QUERYPARAM -> writer.append("ctx.queryParamMap()");
      case FORM, FORMPARAM -> writer.append("ctx.formParamMap()");
      default ->
          throw new UnsupportedOperationException(
              "Only Query/Form Params have Map<String, List<String>> supported in Jex");
    }
  }

  @Override
  public void writeReadCollectionParameter(Append writer, ParamType paramType, String paramName) {
    switch (paramType) {
      case QUERYPARAM -> writer.append("ctx.queryParams(\"%s\")", paramName);
      case FORMPARAM -> writer.append("ctx.formParams(\"%s\")", paramName);
      default ->
          throw new UnsupportedOperationException(
              "Only MultiValue Form/Query Params are supported in Jex");
    }
  }

  @Override
  public void writeReadCollectionParameter(
      Append writer, ParamType paramType, String paramName, List<String> paramDefault) {

    switch (paramType) {
      case QUERYPARAM ->
          writer.append(
              "withDefault(ctx.queryParams(\"%s\"), java.util.List.of(\"%s\"))",
              paramName, String.join(",", paramDefault));
      case FORMPARAM ->
          writer.append(
              "withDefault(ctx.formParams(\"%s\"), java.util.List.of(\"%s\"))",
              paramName, String.join(",", paramDefault));
      default ->
          throw new UnsupportedOperationException(
              "Only MultiValue Form/Query Params are supported in Jex");
    }
  }

  @Override
  public void writeAcceptLanguage(Append writer) {
    writer.append("ctx.header(\"%s\")", Constants.ACCEPT_LANGUAGE);
  }
}
