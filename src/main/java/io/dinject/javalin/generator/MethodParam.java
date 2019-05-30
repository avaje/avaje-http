package io.dinject.javalin.generator;

import javax.lang.model.element.VariableElement;

class MethodParam {

  private final ElementReader elementParam;

  MethodParam(VariableElement param, ProcessingContext ctx, ParamType defaultParamType) {
    this.elementParam = new ElementReader(param, ctx, defaultParamType);
  }

  void buildCtxGet(Append writer, PathSegments segments) {
    elementParam.writeCtxGet(writer, segments);
  }

  void addImports(ControllerReader bean) {
    elementParam.addImports(bean);
  }

  void buildParamName(Append writer) {
    elementParam.writeParamName(writer);
  }
}
