package io.dinject.webroutegen;

import io.dinject.controller.Path;
import io.dinject.controller.Produces;
import io.swagger.v3.oas.annotations.Hidden;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.validation.Valid;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Reads the type information for the Controller (bean).
 */
class ControllerReader {

  private final ProcessingContext ctx;

  private final TypeElement beanType;

  private final List<Element> interfaces;

  private final List<ExecutableElement> interfaceMethods;

  private final List<String> roles;

  private final List<MethodReader> methods = new ArrayList<>();

  private final Set<String> staticImportTypes = new TreeSet<>();

  private final Set<String> importTypes = new TreeSet<>();

  /**
   * The produces media type for the controller. Null implies JSON.
   */
  private final String produces;

  private boolean docHidden;

  private final boolean includeValidator;

  ControllerReader(TypeElement beanType, ProcessingContext ctx) {
    this.beanType = beanType;
    this.ctx = ctx;
    this.interfaces = initInterfaces();
    this.interfaceMethods = initInterfaceMethods();
    this.roles = Util.findRoles(beanType);
    final String generated = ctx.getGeneratedAnnotation();
    if (generated != null) {
      importTypes.add(generated);
    }
    if (ctx.isOpenApiAvailable()) {
      docHidden = initDocHidden();
    }
    includeValidator = initIncludeValidator();
    importTypes.add(Constants.SINGLETON);
    importTypes.add(Constants.API_BUILDER);
    importTypes.add(Constants.IMPORT_CONTROLLER);
    importTypes.add(beanType.getQualifiedName().toString());
    if (includeValidator) {
      importTypes.add(Constants.VALIDATOR);
    }

    this.produces = initProduces();
  }

  private List<Element> initInterfaces() {

    List<Element> interfaces = new ArrayList<>();

    for (TypeMirror anInterface : beanType.getInterfaces()) {
      final Element ifaceElement = ctx.asElement(anInterface);
      if (ifaceElement.getAnnotation(Path.class) != null) {
        interfaces.add(ifaceElement);
      }
    }
    return interfaces;
  }

  private List<ExecutableElement> initInterfaceMethods() {

    List<ExecutableElement> ifaceMethods = new ArrayList<>();
    for (Element anInterface : interfaces) {
      ifaceMethods.addAll(ElementFilter.methodsIn(anInterface.getEnclosedElements()));
    }
    return ifaceMethods;
  }

  private <A extends Annotation> A findAnnotation(Class<A> type) {
    A annotation = beanType.getAnnotation(type);
    if (annotation != null) {
      return annotation;
    }
    for (Element anInterface : interfaces) {
      annotation = anInterface.getAnnotation(type);
      if (annotation != null) {
        return annotation;
      }
    }
    return null;
  }

  <A extends Annotation> A findMethodAnnotation(Class<A> type, ExecutableElement element) {

    for (ExecutableElement interfaceMethod : interfaceMethods) {
      if (matchMethod(interfaceMethod, element)) {
        final A annotation = interfaceMethod.getAnnotation(type);
        if (annotation != null) {
          return annotation;
        }
      }
    }

    return null;
  }

  private boolean matchMethod(ExecutableElement interfaceMethod, ExecutableElement element) {
    return interfaceMethod.toString().equals(element.toString());
  }

  private String initProduces() {
    final Produces produces = findAnnotation(Produces.class);
    return (produces == null) ? null : produces.value();
  }

  private boolean initDocHidden() {
    return findAnnotation(Hidden.class) != null;
  }

  private boolean initIncludeValidator() {
    return findAnnotation(Valid.class) != null;
  }

  String getProduces() {
    return produces;
  }

  TypeElement getBeanType() {
    return beanType;
  }

  boolean isDocHidden() {
    return docHidden;
  }

  boolean isIncludeValidator() {
    return includeValidator;
  }

  void read() {
    if (!roles.isEmpty()) {
      addStaticImportType(Constants.JAVALIN3_ROLES);
      for (String role : roles) {
        addStaticImportType(role);
      }
    }

    for (Element element : beanType.getEnclosedElements()) {
      if (element.getKind() == ElementKind.METHOD) {
        readMethod((ExecutableElement) element);
      }
    }

    readSuper(beanType);
  }

  /**
   * Read methods from superclasses taking into account generics.
   */
  private void readSuper(TypeElement beanType) {

    TypeMirror superclass = beanType.getSuperclass();
    if (superclass.getKind() != TypeKind.NONE) {
      DeclaredType declaredType = (DeclaredType) superclass;

      final Element superElement = ctx.asElement(superclass);
      if (!"java.lang.Object".equals(superElement.toString())) {
        for (Element element : superElement.getEnclosedElements()) {
          if (element.getKind() == ElementKind.METHOD) {
            readMethod((ExecutableElement) element, declaredType);
          }
        }
        if (superElement instanceof TypeElement) {
          readSuper((TypeElement) superElement);
        }
      }
    }
  }

  private void readMethod(ExecutableElement element) {
    readMethod(element, null);
  }

  private void readMethod(ExecutableElement method, DeclaredType declaredType) {

    ExecutableType actualExecutable = null;
    if (declaredType != null) {
      // actual taking into account generics
      actualExecutable = (ExecutableType) ctx.asMemberOf(declaredType, method);
    }

    MethodReader methodReader = new MethodReader(this, method, actualExecutable, ctx);
    if (methodReader.isWebMethod()) {
      methodReader.read();
      methods.add(methodReader);
    }
  }

  List<String> getRoles() {
    return roles;
  }

  List<MethodReader> getMethods() {
    return methods;
  }

  String getPath() {
    Path path = findAnnotation(Path.class);
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
