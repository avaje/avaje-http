package io.dinject.javlin.generator;

import javax.lang.model.element.VariableElement;
import java.util.Set;

class MethodParam {

  private static final String IO_JAVALIN_CONTEXT = "io.javalin.Context";
  private final String rawType;
  private final TypeHandler typeHandler;
  private final String name;


  MethodParam(VariableElement param) {
    this.name = param.getSimpleName().toString();
    this.rawType = param.asType().toString();
    this.typeHandler = TypeMap.get(rawType);
  }

  private boolean isJavlinContext() {
    return IO_JAVALIN_CONTEXT.equals(rawType);
  }

  void buildCtxGet(Append writer, Set<String> pathParams) {

    if (isJavlinContext()) {
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
    boolean isPathParam = pathParams.contains(name);

    String asMethod = (typeHandler == null) ? null : (isPathParam) ? typeHandler.asMethod() : typeHandler.toMethod();

    if (asMethod != null) {
      writer.append(asMethod);
    }
    if (isPathParam) {
      writer.append("ctx.pathParam(\"%s\")", name);

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
    if (isJavlinContext()) {
      writer.append("ctx");
    } else {
      writer.append(name);
    }
  }
}
