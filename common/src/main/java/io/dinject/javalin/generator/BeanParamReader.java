package io.dinject.javalin.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
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

  private final List<ExecutableElement> constructors = new ArrayList<>();

  BeanParamReader(ProcessingContext ctx, TypeElement beanType, String beanVarName, String beanShortType, ParamType defaultParamType) {
    this.ctx = ctx;
    this.beanType = beanType;
    this.beanVarName = beanVarName;
    this.beanShortType = beanShortType;
    this.defaultParamType = defaultParamType;

    read();
  }

  private void read() {

    for (Element enclosedElement : beanType.getEnclosedElements()) {
      switch (enclosedElement.getKind()) {
        case CONSTRUCTOR:
          constructors.add((ExecutableElement) enclosedElement);
          break;
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

    writer.append(" new %s(", beanShortType);
    final Set<String> constructorParams = writeConstructorParams(writer);
    writer.append(");").eol();

    for (String setterMethod : setterMethods) {
      String propName = Util.propertyName(setterMethod);
      if (!constructorParams.contains(propName)) {
        FieldReader field = fieldMap.get(propName);
        if (field != null) {
          field.setUseSetter(setterMethod);
        }
      }
    }

    for (FieldReader field : fieldMap.values()) {
      if (!field.isConstructorParam()) {
        field.writeSet(writer, beanVarName);
      }
    }
    writer.eol();
  }

  private Set<String> writeConstructorParams(Append writer) {
    Set<String> paramsUsed = new HashSet<>();
    if (constructors.size() == 1) {
      int count = 0;
      for (VariableElement parameter : constructors.get(0).getParameters()) {
        final String paramName = parameter.getSimpleName().toString();
        final FieldReader field = fieldMap.get(paramName);
        if (field != null) {
          if (count++ > 0) {
            writer.append(", ");
          }
          writer.eol().append("        ");
          field.writeConstructorParam(writer);
          paramsUsed.add(paramName);
        }
      }
      if (count > 0) {
        writer.eol().append("      ");
      }
    }
    return paramsUsed;
  }

  static class FieldReader {

    private final ElementReader element;

    private String setterMethod;

    private boolean constructorParam;

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

    void writeConstructorParam(Append writer) {
      // populate in constructor
      constructorParam = true;
      element.setValue(writer);
    }

    boolean isConstructorParam() {
      return constructorParam;
    }

    void writeSet(Append writer, String beanVarName) {
      if (setterMethod != null) {
        // populate via setter method
        writer.append("      %s.%s(", beanVarName, setterMethod);
        element.setValue(writer);
        writer.append(");").eol();

      } else {
        // populate via field put
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
