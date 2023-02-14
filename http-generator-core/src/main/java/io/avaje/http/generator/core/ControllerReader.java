package io.avaje.http.generator.core;

import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
/**
 * Reads the type information for the Controller (bean).
 */
public final class ControllerReader {

  private final ProcessingContext ctx;
  private final TypeElement beanType;
  private final List<Element> interfaces;
  private final List<ExecutableElement> interfaceMethods;
  private final List<String> roles;
  private final List<MethodReader> methods = new ArrayList<>();
  private final Set<String> staticImportTypes = new TreeSet<>();
  private final Set<String> importTypes = new TreeSet<>();
  private final List<OpenAPIResponsePrism> apiResponses;

  /** The producesPrism media type for the controller. Null implies JSON. */
  private final String producesPrism;

  private final boolean hasValid;
  private boolean methodHasValid;

  /**
   * Flag set when the controller is dependent on a request scope type.
   */
  private boolean requestScope;
  private boolean docHidden;

  public ControllerReader(TypeElement beanType, ProcessingContext ctx) {
    this.beanType = beanType;
    this.ctx = ctx;
    this.interfaces = initInterfaces();
    this.interfaceMethods = initInterfaceMethods();
    this.roles = buildRoles();
    if (ctx.isOpenApiAvailable()) {
      docHidden = initDocHidden();
    }
    this.hasValid = initHasValid();
    this.producesPrism = initProduces();
    this.apiResponses = buildApiResponses();
  }

  private List<OpenAPIResponsePrism> buildApiResponses() {
    final var responses = new ArrayList<OpenAPIResponsePrism>();
    buildApiResponsesFor(beanType, responses);
    for (final Element anInterface : interfaces) {
      buildApiResponsesFor(anInterface, responses);
    }
    return responses;
  }

  private void buildApiResponsesFor(Element element, ArrayList<OpenAPIResponsePrism> responses) {

    OpenAPIResponsesPrism.getOptionalOn(element).stream()
        .map(OpenAPIResponsesPrism::value)
        .flatMap(List::stream)
        .forEach(responses::add);

    responses.addAll(OpenAPIResponsePrism.getAllInstancesOn(element));
  }

  private ArrayList<String> buildRoles() {
    final var roleList = new ArrayList<>(Util.findRoles(beanType));
    for (final Element anInterface : interfaces) {
      roleList.addAll(Util.findRoles(anInterface));
    }
    return roleList;
  }

  void addImports(boolean withSingleton) {
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
      final var controller = ControllerPrism.getInstanceOn(ifaceElement);
      if (controller != null && !controller.value().isBlank()
          || PathPrism.getInstanceOn(ifaceElement) != null) {
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

  private <A> Optional<A> findAnnotation(Function<Element, Optional<A>> func) {
    var annotation = func.apply(beanType);
    if (annotation.isPresent()) {
      return annotation;
    }
    for (final Element anInterface : interfaces) {
      annotation = func.apply(anInterface);
      if (annotation.isPresent()) {
        return annotation;
      }
    }
    return Optional.empty();
  }

  <A> Optional<A> findMethodAnnotation(Function<Element, Optional<A>> func, ExecutableElement element) {
    for (final ExecutableElement interfaceMethod : interfaceMethods) {
      if (matchMethod(interfaceMethod, element)) {
        final var annotation = func.apply(interfaceMethod);
        if (annotation.isPresent()) {
          return annotation;
        }
      }
    }
    return Optional.empty();
  }

  private boolean matchMethod(ExecutableElement interfaceMethod, ExecutableElement element) {
    return interfaceMethod.toString().equals(element.toString());
  }

  private String initProduces() {
    return findAnnotation(ProducesPrism::getOptionalOn).map(ProducesPrism::value).orElse(null);
  }

  private boolean initDocHidden() {
    return findAnnotation(HiddenPrism::getOptionalOn).isPresent();
  }

  private boolean initHasValid() {

    return findAnnotation(JavaxValidPrism::getOptionalOn).isPresent()
        || findAnnotation(ValidPrism::getOptionalOn).isPresent();
  }

  String produces() {
    return producesPrism;
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

  public List<OpenAPIResponsePrism> openApiResponses() {
    return apiResponses;
  }

  public String path() {

    return findAnnotation(ControllerPrism::getOptionalOn)
        .map(ControllerPrism::value)
        .filter(not(String::isBlank))
        .or(() -> findAnnotation(PathPrism::getOptionalOn).map(PathPrism::value))
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
