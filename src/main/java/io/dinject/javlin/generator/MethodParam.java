package io.dinject.javlin.generator;

import javax.lang.model.element.VariableElement;
import java.util.Set;

class MethodParam {

  private final String rawType;
  private final TypeHandler typeHandler;
  private final String name;
  private final String snakeName;


  MethodParam(VariableElement param) {
    this.name = param.getSimpleName().toString();
    this.snakeName = Util.snakeCase(name);
    this.rawType = param.asType().toString();
    this.typeHandler = TypeMap.get(rawType);
  }

  private boolean isJavalinContext() {
    return Constants.IO_JAVALIN_CONTEXT.equals(rawType);
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

    writer.append("      %s %s = ", shortType, name);

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
        writer.append("ctx.bodyAsClass(%s.class)", shortType, name, shortType);
      } else {
        writer.append("ctx.queryParam(\"%s\")", name);
      }
    }

    if (asMethod != null) {
      writer.append(")");
    }
    writer.append(";").eol();
  }

  private String derivePathParam(Set<String> pathParams) {
    if (pathParams.contains(name)) {
      return name;
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
      writer.append(name);
    }
  }
}
