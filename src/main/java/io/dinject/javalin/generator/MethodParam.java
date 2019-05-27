package io.dinject.javalin.generator;

import io.dinject.controller.Cookie;
import io.dinject.controller.Default;
import io.dinject.controller.FormParam;
import io.dinject.controller.Header;
import io.dinject.controller.QueryParam;

import javax.lang.model.element.VariableElement;
import java.util.Set;

class MethodParam {

  private final String rawType;
  private final TypeHandler typeHandler;
  private final String varName;
  private final String snakeName;
  private final String docComment;
  private String paramName;
  private String paramDefault;
  private String paramType;

  MethodParam(VariableElement param, ProcessingContext ctx) {
    this.rawType = param.asType().toString();
    this.typeHandler = TypeMap.get(rawType);
    this.varName = param.getSimpleName().toString();
    this.snakeName = Util.snakeCase(varName);
    this.paramName = varName;
    this.docComment = ctx.docComment(param);

    Default defaultVal = param.getAnnotation(Default.class);
    if (defaultVal != null) {
      this.paramDefault = defaultVal.value();
    }

    QueryParam queryParam = param.getAnnotation(QueryParam.class);
    if (queryParam != null) {
      this.paramName = nameFrom(queryParam.value(), varName);
      this.paramType = "queryParam";
    }
    FormParam formParam = param.getAnnotation(FormParam.class);
    if (formParam != null) {
      this.paramName = nameFrom(formParam.value(), varName);
      this.paramType = "formParam";
    }
    Cookie cookieParam = param.getAnnotation(Cookie.class);
    if (cookieParam != null) {
      this.paramName = nameFrom(cookieParam.value(), varName);
      this.paramType = "cookie";
      this.paramDefault = null;
    }
    Header headerParam = param.getAnnotation(Header.class);
    if (headerParam != null) {
      this.paramName = nameFrom(headerParam.value(), Util.initcapSnake(snakeName));
      this.paramType = "header";
      this.paramDefault = null;
    }
    if (paramType == null) {
      this.paramType = "queryParam";
    }
  }

  private String nameFrom(String name, String defaultName) {
    if (name != null && !name.isEmpty()) {
      return name;
    }
    return defaultName;
  }

  private boolean hasParamDefault() {
    return paramDefault != null && !paramDefault.isEmpty();
  }

  private boolean isJavalinContext() {
    return Constants.JAVALIN_CONTEXT.equals(rawType);
  }

  void buildCtxGet(Append writer, Set<String> pathParams) {

    if (isJavalinContext()) {
      // no conversion for this parameter
      return;
    }

    String shortType;
    if (typeHandler != null) {
      shortType = typeHandler.shortName();
    } else {
      shortType = Util.shortName(rawType);
    }

    writer.append("      %s %s = ", shortType, varName);

    // path parameters are expected to be not nullable
    // ... with query parameters nullable
    String pathParameter = derivePathParam(pathParams);
    String asMethod = (typeHandler == null) ? null : (pathParameter != null) ? typeHandler.asMethod() : typeHandler.toMethod();

    if (asMethod != null) {
      writer.append(asMethod);
    }
    if (pathParameter != null) {
      writer.append("ctx.pathParam(\"%s\")", pathParameter);
    } else {
      if (typeHandler == null) {
        // assuming this is a body (POST, PATCH)
        writer.append("ctx.bodyAsClass(%s.class)", shortType);
      } else {
        if (hasParamDefault()) {
          writer.append("ctx.%s(\"%s\",\"%s\")", paramType, paramName, paramDefault);
        } else {
          writer.append("ctx.%s(\"%s\")", paramType, paramName);
        }
      }
    }

    if (asMethod != null) {
      writer.append(")");
    }
    writer.append(";").eol();
  }

  private String derivePathParam(Set<String> pathParams) {
    if (pathParams.contains(varName)) {
      return varName;
    }
    if (pathParams.contains(snakeName)){
      return snakeName;
    }
    return null;
  }

  void addImports(BeanReader bean) {
    if (typeHandler != null) {
      String importType = typeHandler.getImportType();
      if (importType != null) {
        bean.addImportType(rawType);
      }
    } else {
      bean.addImportType(rawType);
    }
  }

  void buildParamName(Append writer) {
    if (isJavalinContext()) {
      writer.append("ctx");
    } else {
      writer.append(varName);
    }
  }
}
