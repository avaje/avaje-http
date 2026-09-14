package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ProcessingContext.*;
import javax.lang.model.element.*;
import java.util.*;

public class BeanParamReader {

  static String toJavaIdentifier(String value) {
    final StringBuilder result = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      result.append(
          i == 0
              ? Character.isJavaIdentifierStart(c) ? c : '_'
              : Character.isJavaIdentifierPart(c) ? c : '_');
    }
    return result.toString();
  }

  private final String beanVarName;
  private final String beanShortType;
  private final TypeElement beanType;
  private final ParamType defaultParamType;
  private final String prefix;
  private final Set<String> setterMethods = new HashSet<>();
  private final Map<String, FieldReader> fieldMap = new LinkedHashMap<>();
  private final List<ExecutableElement> constructors = new ArrayList<>();
  private final Map<String, ExecutableElement> methodMap = new LinkedHashMap<>();
  private final Set<String> imports = new HashSet<>();

  public BeanParamReader(TypeElement beanType, String beanVarName, String beanShortType, ParamType defaultParamType) {
    this(beanType, beanVarName, beanShortType, defaultParamType, "");
  }

  public BeanParamReader(TypeElement beanType, String beanVarName, String beanShortType, ParamType defaultParamType, String prefix) {
    this.beanType = beanType;
    this.beanVarName = beanVarName;
    this.beanShortType = beanShortType;
    this.defaultParamType = defaultParamType;
    this.prefix = prefix;
    read();
  }

  public Collection<String> imports() {
    return imports;
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
    if (IgnorePrism.isPresent(enclosedElement)) {
      return;
    }
    FieldReader field = new FieldReader(enclosedElement, defaultParamType, prefix);
    fieldMap.put(field.varName(), field);

    imports.addAll(UType.parse(field.element.element().asType()).importTypes());
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
    writeConstructorParams(writer);
    writer.append(");").eol();
    writeFields(writer);
  }

  /**
   * Write the creation of nested form objects that are constructor parameters of this bean,
   * before the bean itself is constructed (eg a record whose component is a {@code @FormPrefix} bean).
   */
  void writeCreateNestedCtorParams(Append writer) {
    if (constructors.size() == 1) {
      for (VariableElement parameter : constructors.get(0).getParameters()) {
        final String paramName = parameter.getSimpleName().toString();
        final FieldReader field = fieldMap.get(paramName);
        if (field != null && field.isNestedForm()) {
          field.writeNestedCreation(writer);
          field.markConstructorParam();
        }
      }
    }
  }

  /**
   * Write the population of all fields against {@code beanVarName} (no constructor call).
   */
  void writeFields(Append writer) {
    for (String setterMethod : setterMethods) {
      String propName = Util.propertyName(setterMethod);
      FieldReader field = fieldMap.get(propName);
      if (field != null && !field.isConstructorParam()) {
        field.setUseSetter(setterMethod);
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
          if (field.isNestedForm()) {
            writer.append(field.nestedVarName());
          } else {
            field.writeConstructorParam(writer);
          }
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
        String accessor = (getter != null)
          ? getter.getSimpleName() + "()"
          : field.isPublic() ? field.varName() : null;
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

    FieldReader(Element enclosedElement, ParamType defaultParamType, String prefix) {
      this.element = new ElementReader(enclosedElement, defaultParamType, false, prefix);
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

    void markConstructorParam() {
      constructorParam = true;
    }

    boolean isConstructorParam() {
      return constructorParam;
    }

    void writeSet(Append writer, String beanVarName) {
      if (element.isNestedForm()) {
        writeNestedSet(writer, beanVarName);
        return;
      }
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

    boolean isNestedForm() {
      return element.isNestedForm();
    }

    /**
     * Unique local variable name for the nested instance, derived from the full
     * prefix path so that the same field name used under different prefixes
     * (eg {@code invoice.zip} and {@code shipping.zip}) does not collide.
     */
    private String nestedVarName() {
      final String p = element.prefix();
      if (p.isEmpty() || p.indexOf('.') == -1) {
        return element.varName();
      }
      final StringBuilder sb = new StringBuilder();
      for (final String part : p.split("\\.")) {
        sb.append(part.isEmpty() ? "" : Character.toUpperCase(part.charAt(0)) + part.substring(1));
      }
      return BeanParamReader.toJavaIdentifier(Character.toLowerCase(sb.charAt(0)) + sb.substring(1));
    }

    /**
     * Write {@code var x = new NestedType(...);} for a {@code @FormPrefix} object, populating via
     * constructor when the nested type is a record (or single constructor) and via no-arg plus
     * field/setter population otherwise.
     */
    void writeNestedCreation(Append writer) {
      final String nestedType = element.shortType();
      final TypeElement nestedTypeElement = typeElement(element.rawType());
      final BeanParamReader nested =
        new BeanParamReader(nestedTypeElement, nestedVarName(), nestedType, ParamType.FORMPARAM, element.prefix());
      nested.writeCreateNestedCtorParams(writer);
      writer.append("%s  var %s =", platform().indent(), nestedVarName());
      nested.write(writer);
    }

    private void writeNestedSet(Append writer, String beanVarName) {
      writeNestedCreation(writer);
      if (setterMethod != null) {
        writer.append("%s  %s.%s(%s);", platform().indent(), beanVarName, setterMethod, nestedVarName()).eol();
      } else {
        writer.append("%s  %s.%s = %s;", platform().indent(), beanVarName, element.varName(), nestedVarName()).eol();
      }
    }
  }

}
