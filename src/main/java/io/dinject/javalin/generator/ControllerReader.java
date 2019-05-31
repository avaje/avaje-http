package io.dinject.javalin.generator;

import io.dinject.controller.Path;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static io.dinject.javalin.generator.Constants.JAVALIN_ROLES;

/**
 * Reads the type information for the Controller (bean).
 */
class ControllerReader {

  private final ProcessingContext ctx;

  private final TypeElement beanType;

  private final List<String> roles;

  private final List<MethodReader> methods = new ArrayList<>();

  private final Set<String> staticImportTypes = new TreeSet<>();

  private final Set<String> importTypes = new TreeSet<>();

  ControllerReader(TypeElement beanType, ProcessingContext ctx) {
    this.beanType = beanType;
    this.ctx = ctx;
    this.roles = Util.findRoles(beanType);
    if (ctx.isGeneratedAvailable()) {
      importTypes.add(Constants.GENERATED);
    }
    importTypes.add(Constants.SINGLETON);
    importTypes.add(Constants.API_BUILDER);
    importTypes.add(Constants.IMPORT_CONTROLLER);
    importTypes.add(beanType.getQualifiedName().toString());
  }

  TypeElement getBeanType() {
    return beanType;
  }

  void read() {
    if (!roles.isEmpty()) {
      addStaticImportType(JAVALIN_ROLES);
      for (String role : roles) {
        addStaticImportType(role);
      }
    }

    for (Element element : beanType.getEnclosedElements()) {
      if (element.getKind() == ElementKind.METHOD) {
        readMethod(element);
      }
    }
  }


  private void readMethod(Element element) {

    ExecutableElement methodElement = (ExecutableElement) element;
    MethodReader methodReader = new MethodReader(this, methodElement, ctx);
    methodReader.read();
    methods.add(methodReader);
  }

  List<String> getRoles() {
    return roles;
  }

  List<MethodReader> getMethods() {
    return methods;
  }

  String getPath() {
    Path path = beanType.getAnnotation(Path.class);
    if (path == null) {
      return null;
    }
    return Util.trimPath(path.value());
  }

  void addImportType(String rawType) {
    importTypes.add(rawType);
  }

  void addStaticImportType(String rawType) {
    staticImportTypes.add(rawType);
  }

  Set<String> getStaticImportTypes() {
    return staticImportTypes;
  }

  Set<String> getImportTypes() {
    return importTypes;
  }

}
