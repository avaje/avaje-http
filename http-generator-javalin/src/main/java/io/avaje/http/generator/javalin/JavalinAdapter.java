package io.avaje.http.generator.javalin;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.lang.model.element.Element;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.CustomWebMethod;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.http.generator.core.UType;

class JavalinAdapter implements PlatformAdapter {

  static final String JAVALIN3_CONTEXT = "io.javalin.http.Context";

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
      if (ProcessingContext.useJsonb()) {
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

    switch (paramType) {
      case QUERYPARAM:
        writer.append("ctx.queryParamMap()");
        break;
      case FORM:
      case FORMPARAM:
        writer.append("ctx.formParamMap()");
        break;
      default:
        throw new UnsupportedOperationException(
            "Only Query/Form Params have Map<String, List<String>> supported in Javalin");
    }
  }

  @Override
  public void writeReadCollectionParameter(Append writer, ParamType paramType, String paramName) {
    switch (paramType) {
      case QUERYPARAM:
        writer.append("ctx.queryParams(\"%s\")", paramName);
        break;
      case FORMPARAM:
        writer.append("ctx.formParams(\"%s\")", paramName);
        break;
      default:
        throw new UnsupportedOperationException(
            "Only MultiValue Form/Query Params are supported in Javalin");
    }
  }

  @Override
  public void writeReadCollectionParameter(
      Append writer, ParamType paramType, String paramName, List<String> paramDefault) {

    switch (paramType) {
      case QUERYPARAM:
        writer.append(
            "withDefault(ctx.queryParams(\"%s\"), java.util.List.of(\"%s\"))",
            paramName, String.join(",", paramDefault));
        break;
      case FORMPARAM:
        writer.append(
            "withDefault(ctx.formParams(\"%s\"), java.util.List.of(\"%s\"))",
            paramName, String.join(",", paramDefault));
        break;
      default:
        throw new UnsupportedOperationException(
            "Only MultiValue Form/Query Params are supported in Javalin");
    }
  }

  @Override
  public void writeAcceptLanguage(Append writer) {
    writer.append("ctx.header(\"%s\")", Constants.ACCEPT_LANGUAGE);
  }

  @Override
  public List<Function<Element, Optional<CustomWebMethod>>> customHandlers() {
    final Function<Element, AfterPrism> f = AfterPrism::getInstanceOn;
    final Function<Element, BeforePrism> f2 = BeforePrism::getInstanceOn;

    return List.of(f.andThen(Optional::ofNullable), f2.andThen(Optional::ofNullable));
  }
}
