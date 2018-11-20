package io.kanuka.web.javlin;

import io.dinject.controller.Path;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Reads the type information for the Controller (bean).
 */
class BeanReader {

  private final TypeElement beanType;

  private final ProcessingContext ctx;

  private String name;

  private final List<MethodReader> methods = new ArrayList<>();

  private final Set<String> importTypes = new TreeSet<>();

  BeanReader(TypeElement beanType, ProcessingContext ctx) {
    this.beanType = beanType;
    this.ctx = ctx;

    importTypes.add(Constants.GENERATED);
    importTypes.add(Constants.SINGLETON);
    importTypes.add(Constants.API_BUILDER);
    importTypes.add(Constants.WEB_ROUTES);
    importTypes.add(beanType.getQualifiedName().toString());
  }

  TypeElement getBeanType() {
    return beanType;
  }

//  String getName() {
//    return name;
//  }

  void read() {
    for (Element element : beanType.getEnclosedElements()) {
      ElementKind kind = element.getKind();
      switch (kind) {
        case METHOD:
          readMethod(element);
          break;
      }
    }
  }


  private void readMethod(Element element) {

    ExecutableElement methodElement = (ExecutableElement) element;
    MethodReader methodReader = new MethodReader(this, methodElement);
    methodReader.read();
    methods.add(methodReader);
  }


  List<MethodReader> getMethods() {
    return methods;
  }

  ProcessingContext getContext() {
    return ctx;
  }

  String getPath() {
    Path path = beanType.getAnnotation(Path.class);
    if (path == null) {
      return null;
    }
    return Util.trimTrailingSlash(path.value());
  }

  void addImportType(String rawType) {
    importTypes.add(rawType);
  }

  Set<String> getImportTypes() {
    return importTypes;
  }
}
