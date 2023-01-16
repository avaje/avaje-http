package io.avaje.http.generator.core;

import static java.util.function.Predicate.not;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

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

import io.avaje.http.api.Controller;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.swagger.v3.oas.annotations.Hidden;

/**
 * Reads the type information for the Controller (bean).
 */
public class ControllerReader {

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
  private final boolean hasValid;
  private boolean methodHasValid;

  /**
   * Flag set when the controller is dependant on a request scope type.
   */
  private boolean requestScope;
  private boolean docHidden;

  public ControllerReader(TypeElement beanType, ProcessingContext ctx) {
    this.beanType = beanType;
    this.ctx = ctx;
    this.interfaces = initInterfaces();
    this.interfaceMethods = initInterfaceMethods();
    this.roles = Util.findRoles(beanType);
    if (ctx.isOpenApiAvailable()) {
      docHidden = initDocHidden();
    }
    this.hasValid = initHasValid();
    this.produces = initProduces();
  }

  protected void addImports(boolean withSingleton) {
    importTypes.add(Constants.IMPORT_HTTP_API);
    importTypes.add(beanType.getQualifiedName().toString());
    if (hasValid || methodHasValid) {
      importTypes.add(Constants.VALIDATOR);
    }
    if (withSingleton) {
      if (ctx.useComponent()) {
        importTypes.add(Constants.COMPONENT);
      } else {
        importTypes.add(ctx.useJavax() ? Constants.SINGLETON_JAVAX : Constants.SINGLETON_JAKARTA);
      }
    }
  }

  private List<Element> initInterfaces() {
    List<Element> interfaces = new ArrayList<>();
    for (TypeMirror anInterface : beanType.getInterfaces()) {
      final Element ifaceElement = ctx.asElement(anInterface);
      var controller = ifaceElement.getAnnotation(Controller.class);
      if (controller != null && !controller.value().isBlank()
          || ifaceElement.getAnnotation(Path.class) != null) {
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

  private boolean initHasValid() {
    Annotation jakarta = null;
    try {
      var anno =
          (Class<Annotation>)
              Class.forName(Valid.class.getCanonicalName().replace("javax", "jakarta"));
      jakarta = findAnnotation(anno);
    } catch (final ClassNotFoundException e) {

    }

    return findAnnotation(Valid.class) != null || jakarta != null;
  }

  String produces() {
    return produces;
  }

  public TypeElement beanType() {
    return beanType;
  }

  public boolean isDocHidden() {
    return docHidden;
  }

  public boolean isIncludeValidator() {
    return hasValid || methodHasValid;
  }

  public boolean hasValid() {
    return hasValid;
  }

  /**
   * Return true if the controller has request scoped dependencies.
   * In that case a BeanFactory will have been generated.
   */
  boolean isRequestScoped() {
    return requestScope;
  }

  public void read(boolean withSingleton) {
    if (!roles.isEmpty()) {
      ctx.platform().controllerRoles(roles, this);
    }
    for (Element element : beanType.getEnclosedElements()) {
      if (element.getKind() == ElementKind.METHOD) {
        readMethod((ExecutableElement) element);
      } else if (element.getKind() == ElementKind.FIELD) {
        readField(element);
      }
    }
    readSuper(beanType);
    deriveIncludeValidation();
    addImports(withSingleton);
  }

  private void deriveIncludeValidation() {
    methodHasValid = methodHasValid();
  }

  private boolean methodHasValid() {
    for (MethodReader method : methods) {
      if (method.hasValid()) {
        return true;
      }
    }
    return false;
  }

  private void readField(Element element) {
    if (!requestScope) {
      final String rawType = element.asType().toString();
      requestScope = RequestScopeTypes.isRequestType(rawType);
    }
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
          } else if (element.getKind() == ElementKind.FIELD) {
            readField(element);
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

  public List<String> roles() {
    return roles;
  }

  public List<MethodReader> methods() {
    return methods;
  }

  public String path() {

    return Optional.ofNullable(findAnnotation(Controller.class))
        .map(Controller::value)
        .filter(not(String::isBlank))
        .or(() -> Optional.ofNullable(findAnnotation(Path.class)).map(Path::value))
        .map(Util::trimPath)
        .orElse(null);
  }

  public void addImportType(String rawType) {
    if (rawType.indexOf('.') > 0) {
      importTypes.add(rawType);
    }
  }

  public void addImportTypes(Set<String> types) {
    for (String type : types) {
      addImportType(type);
    }
  }

  public void addStaticImportType(String rawType) {
    staticImportTypes.add(rawType);
  }

  public Set<String> staticImportTypes() {
    return staticImportTypes;
  }

  public Set<String> importTypes() {
    return importTypes;
  }

}
