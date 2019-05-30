package io.dinject.javalin.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class BeanParamReader {

  private final ProcessingContext ctx;
  private final String beanVarName;
  private final String beanShortType;
  private final TypeElement beanType;

  private final ParamType defaultParamType;

  private final Set<String> setterMethods = new HashSet<>();

  private final Map<String, FieldReader> fieldMap = new LinkedHashMap<>();

  BeanParamReader(ProcessingContext ctx, TypeElement beanType, String beanVarName, String beanShortType, ParamType defaultParamType) {
    this.ctx = ctx;
    this.beanType = beanType;
    this.beanVarName = beanVarName;
    this.beanShortType = beanShortType;
    this.defaultParamType = defaultParamType;

    read();
  }

  private void read() {

    final List<? extends Element> enclosedElements = beanType.getEnclosedElements();
    for (Element enclosedElement : enclosedElements) {
      switch (enclosedElement.getKind()) {
        case METHOD:
          readMethod(enclosedElement);
          break;
        case FIELD:
          readField(enclosedElement);
          break;
      }
    }
  }

  private void readField(Element enclosedElement) {

    FieldReader field = new FieldReader(ctx, enclosedElement, defaultParamType);
    fieldMap.put(field.getVarName(), field);
  }

  private void readMethod(Element enclosedElement) {
    String simpleName = enclosedElement.getSimpleName().toString();
    if (simpleName.startsWith("set")) {
      setterMethods.add(simpleName);
    }
  }

  void write(Append writer) {

    writer.append(" new %s();", beanShortType).eol();

    for (String setterMethod : setterMethods) {
      String propName = Util.propertyName(setterMethod);
      FieldReader field = fieldMap.get(propName);
      if (field != null) {
        field.setUseSetter(setterMethod);
      }
    }

    for (FieldReader field : fieldMap.values()) {
      field.writeSet(writer, beanVarName);
    }
    writer.eol();
  }

  static class FieldReader {

    private final ElementReader element;

    private String setterMethod;

    FieldReader(ProcessingContext ctx, Element enclosedElement, ParamType defaultParamType) {
      this.element = new ElementReader(enclosedElement, ctx, defaultParamType, false);
    }

    String getVarName() {
      return element.getVarName();
    }

    @Override
    public String toString() {
      return element.toString();
    }

    void writeSet(Append writer, String beanVarName) {
      if (setterMethod != null) {
        writer.append("      %s.%s(", beanVarName, setterMethod);
        element.setValue(writer);
        writer.append(");").eol();

      } else {
        writer.append("      %s.%s = ", beanVarName, getVarName());
        element.setValue(writer);
        writer.append(";").eol();
      }
    }

    void setUseSetter(String setterMethod) {
      this.setterMethod = setterMethod;
    }
  }


}
