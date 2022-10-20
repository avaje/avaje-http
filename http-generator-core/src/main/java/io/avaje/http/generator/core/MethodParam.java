package io.avaje.http.generator.core;

import io.avaje.http.generator.core.openapi.MethodDocBuilder;
import javax.lang.model.element.VariableElement;

public class MethodParam {

  private final ElementReader elementParam;

  MethodParam(
      VariableElement param,
      UType type,
      String rawType,
      ProcessingContext ctx,
      ParamType defaultParamType,
      boolean formMarker) {
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
    return elementParam.paramType() == ParamType.BODY;
  }

  public boolean isForm() {
    return elementParam.paramType() == ParamType.FORM;
  }

  public String shortType() {
    return elementParam.shortType();
  }

  public String rawType() {
    return elementParam.rawType();
  }

  public String name() {
    return elementParam.varName();
  }

  public String paramName() {
    return elementParam.paramName();
  }

  public ParamType paramType() {
    return elementParam.paramType();
  }

  public UType utype() {
    return elementParam.type();
  }

  public void setResponseHandler() {
    elementParam.setResponseHandler();
  }
}
