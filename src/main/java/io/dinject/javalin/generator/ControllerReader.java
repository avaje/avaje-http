package io.dinject.javalin.generator;

import io.dinject.controller.Path;
import io.dinject.controller.Produces;
import io.swagger.v3.oas.annotations.Hidden;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.lang.annotation.Annotation;
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

  ControllerReader(TypeElement beanType, ProcessingContext ctx) {
    this.beanType = beanType;
    this.ctx = ctx;
    this.interfaces = initInterfaces();
    this.interfaceMethods = initInterfaceMethods();
    this.roles = Util.findRoles(beanType);
    if (ctx.isGeneratedAvailable()) {
      importTypes.add(Constants.GENERATED);
    }
    if (ctx.isOpenApiAvailable()) {
      docHidden = initDocHidden();
    }
    importTypes.add(Constants.SINGLETON);
    importTypes.add(Constants.API_BUILDER);
    importTypes.add(Constants.IMPORT_CONTROLLER);
    importTypes.add(beanType.getQualifiedName().toString());

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

  <A extends Annotation> A findAnnotation(Class<A> type) {
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
          ctx.logDebug("found interface method annotation : " + annotation + " 2:" + interfaceMethod);
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

  String getProduces() {
    return produces;
  }

  TypeElement getBeanType() {
    return beanType;
  }

  boolean isDocHidden() {
    return docHidden;
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
