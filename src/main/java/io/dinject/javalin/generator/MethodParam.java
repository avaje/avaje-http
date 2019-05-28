package io.dinject.javalin.generator;

import javax.lang.model.element.VariableElement;
import java.util.Set;

class MethodParam {

  private final ElementReader elementParam;

  MethodParam(VariableElement param, ProcessingContext ctx) {
    this.elementParam = new ElementReader(param, ctx, ParamType.QUERYPARAM);
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
