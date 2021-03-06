package io.avaje.http.generator.core;

import io.avaje.http.generator.core.openapi.MethodDocBuilder;

import javax.lang.model.element.VariableElement;

public class MethodParam {

  private final ElementReader elementParam;

  MethodParam(VariableElement param, String rawType, ProcessingContext ctx, ParamType defaultParamType, boolean formMarker) {
    this.elementParam = new ElementReader(param, rawType, ctx, defaultParamType, formMarker);
  }

  public void writeCtxGet(Append writer, PathSegments segments) {
    elementParam.writeCtxGet(writer, segments);
  }

  public void addImports(ControllerReader bean) {
    elementParam.addImports(bean);
  }

  public void writeValidate(Append writer) {
    elementParam.writeValidate(writer);
  }

  public void buildParamName(Append writer) {
    elementParam.writeParamName(writer);
  }

  public void buildApiDocumentation(MethodDocBuilder methodDoc) {
    elementParam.buildApiDocumentation(methodDoc);
  }

  public boolean isBody() {
    return elementParam.getParamType() == ParamType.BODY;
  }

  public boolean isForm() {
    return elementParam.getParamType() == ParamType.FORM;
  }

  public String getShortType() {
    return elementParam.getShortType();
  }

  public String getName() {
    return elementParam.getVarName();
  }

}
