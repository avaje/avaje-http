package io.dinject.javalin.generator;

import javax.lang.model.element.VariableElement;
import java.util.Set;

class MethodParam {

  private final ElementReader elementParam;

  MethodParam(VariableElement param, ProcessingContext ctx, ParamType defaultParamType) {
    this.elementParam = new ElementReader(param, ctx, defaultParamType);
  }

  void buildCtxGet(Append writer, Set<String> pathParams) {

    elementParam.writeCtxGet(writer, pathParams);
  }

  void addImports(ControllerReader bean) {
    elementParam.addImports(bean);
  }

  void buildParamName(Append writer) {
    elementParam.writeParamName(writer);
  }
}
