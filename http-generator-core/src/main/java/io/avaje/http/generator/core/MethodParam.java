package io.avaje.http.generator.core;

import io.avaje.http.generator.core.openapi.MethodDocBuilder;

import javax.lang.model.element.VariableElement;

public class MethodParam {

  private final ElementReader elementParam;

  MethodParam(VariableElement param, UType type, String rawType, ProcessingContext ctx, ParamType defaultParamType, boolean formMarker) {
    this.elementParam = new ElementReader(param, type, rawType, ctx, defaultParamType, formMarker);
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

  public String getRawType() {
    return elementParam.getRawType();
  }

  public String getName() {
    return elementParam.getVarName();
  }

  public String getParamName() {
    return elementParam.getParamName();
  }

  public ParamType getParamType() {
    return elementParam.getParamType();
  }

  public UType getUType() {
    return elementParam.getType();
  }

  public void setResponseHandler() {
    elementParam.setResponseHandler();
  }
}
