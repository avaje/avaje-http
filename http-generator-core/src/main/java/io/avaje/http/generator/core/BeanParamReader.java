package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ProcessingContext.*;
import javax.lang.model.element.*;
import java.util.*;

public class BeanParamReader {

  private final String beanVarName;
  private final String beanShortType;
  private final TypeElement beanType;
  private final ParamType defaultParamType;
  private final Set<String> setterMethods = new HashSet<>();
  private final Map<String, FieldReader> fieldMap = new LinkedHashMap<>();
  private final List<ExecutableElement> constructors = new ArrayList<>();
  private final Map<String, ExecutableElement> methodMap = new LinkedHashMap<>();

  public BeanParamReader(TypeElement beanType, String beanVarName, String beanShortType, ParamType defaultParamType) {
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
          readMethod((ExecutableElement) enclosedElement);
          break;
        case FIELD:
          readField(enclosedElement);
          break;
      }
    }
  }

  private void readField(Element enclosedElement) {
    FieldReader field = new FieldReader(enclosedElement, defaultParamType);
    fieldMap.put(field.varName(), field);
  }

  private void readMethod(ExecutableElement enclosedElement) {
    String simpleName = enclosedElement.getSimpleName().toString();
    if (enclosedElement.getParameters().isEmpty()) {
      // getter methods
      methodMap.put(simpleName, enclosedElement);
    }
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

  public void writeFormParams(Append writer) {
    for (FieldReader field : fieldMap.values()) {
      ExecutableElement getter = findGetter(field.varName());
      ParamType paramType = field.element.paramType();
      String type = propertyParamType(paramType);
      if (type != null) {
        String accessor = (getter != null) ? getter.toString() : field.isPublic() ? field.varName() : null;
        if (accessor != null) {
          writer.append("      .%s(\"%s\", %s.%s)", type, field.paramName(), beanVarName, accessor).eol();
        }
      }
    }
  }

  private String propertyParamType(ParamType paramType) {
    switch (paramType) {
      case FORMPARAM:
      case QUERYPARAM:
      case HEADER:
        return paramType.toString();
      default:
        return null;
    }
  }

  private ExecutableElement findGetter(String varName) {
    ExecutableElement getter = methodMap.get(varName);
    if (getter == null) {
      String initCap = Util.initcapSnake(varName);
      getter = methodMap.get("get" + initCap);
      if (getter == null) {
        getter = methodMap.get("is" + initCap);
      }
    }
    return getter;
  }

  static class FieldReader {

    private final ElementReader element;
    private String setterMethod;
    private boolean constructorParam;

    FieldReader(Element enclosedElement, ParamType defaultParamType) {
      this.element = new ElementReader(enclosedElement, defaultParamType, false);
    }

    boolean isPublic() {
      return element.element().getModifiers().contains(Modifier.PUBLIC);
    }

    String paramName() {
      return element.paramName();
    }

    String varName() {
      return element.varName();
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
        writer.append("%s  %s.%s(", platform().indent(), beanVarName, setterMethod);
        element.setValue(writer);
        writer.append(");").eol();

      } else {
        // populate via field put
        writer.append("%s  %s.%s = ", platform().indent(), beanVarName, varName());
        element.setValue(writer);
        writer.append(";").eol();
      }
    }

    void setUseSetter(String setterMethod) {
      this.setterMethod = setterMethod;
    }
  }

}
