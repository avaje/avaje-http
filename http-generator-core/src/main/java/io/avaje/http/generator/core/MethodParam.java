package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ProcessingContext.asElement;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;

import io.avaje.http.generator.core.openapi.MethodDocBuilder;

public class MethodParam {

  private final ElementReader elementParam;

  MethodParam(VariableElement param, UType type, String rawType, ParamType defaultParamType, boolean formMarker) {
    this.elementParam = new ElementReader(param, type, rawType, defaultParamType, formMarker);
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
    if (elementParam.paramType() != ParamType.BEANPARAM && elementParam.paramType() != ParamType.FORM) {
      elementParam.buildApiDocumentation(methodDoc);
    } else {
      asElement(elementParam.element().asType()).getEnclosedElements().stream()
          .filter(e -> e.getKind() == ElementKind.FIELD)
          .map(VariableElement.class::cast)
          .forEach(e -> buildDoc(methodDoc, e, elementParam.paramType()==ParamType.FORM));
    }
  }

  private static void buildDoc(MethodDocBuilder methodDoc, VariableElement e, boolean form) {
    final var typeMirror = e.asType();
    new ElementReader(e, Util.parse(typeMirror.toString()), Util.typeDef(typeMirror), form?ParamType.FORMPARAM:ParamType.QUERYPARAM, form)
      .buildApiDocumentation(methodDoc);
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

  public boolean overrideVarNameError() {
    return elementParam.overrideVarNameError();
  }

  public void overrideVarName(String name, ParamType paramType) {
    elementParam.overrideVarName(name, paramType);
  }

  public void overrideVarName(int position) {
    elementParam.overrideVarName(position);
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

  public VariableElement element() {
    return (VariableElement) elementParam.element();
  }

  @Override
  public String toString() {
    return elementParam.toString();
  }
}
