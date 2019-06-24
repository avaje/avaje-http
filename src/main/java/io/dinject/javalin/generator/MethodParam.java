package io.dinject.javalin.generator;

import io.dinject.javalin.generator.javadoc.Javadoc;
import io.swagger.v3.oas.models.Operation;

import javax.lang.model.element.VariableElement;

class MethodParam {

  private final ElementReader elementParam;

  MethodParam(VariableElement param, String rawType, ProcessingContext ctx, ParamType defaultParamType, boolean formMarker) {
    this.elementParam = new ElementReader(param, rawType, ctx, defaultParamType, formMarker);
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

  void addMeta(ProcessingContext ctx, Javadoc javadoc, Operation operation) {
    elementParam.addMeta(ctx, javadoc, operation);
  }
}
