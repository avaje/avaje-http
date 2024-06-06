package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ProcessingContext.asElement;
import static io.avaje.http.generator.core.ProcessingContext.asMemberOf;
import static io.avaje.http.generator.core.ProcessingContext.instrumentAllWebMethods;
import static io.avaje.http.generator.core.ProcessingContext.isOpenApiAvailable;
import static io.avaje.http.generator.core.ProcessingContext.platform;
import static io.avaje.http.generator.core.ProcessingContext.useComponent;
import static io.avaje.http.generator.core.ProcessingContext.useJavax;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.HashSet;
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

  private final TypeElement beanType;
  private final List<TypeElement> interfaces;
  private final List<ExecutableElement> interfaceMethods;
  private final List<String> roles;
  private final List<MethodReader> methods = new ArrayList<>();
  private final Set<String> seenMethods = new HashSet<>();
  private final Set<String> staticImportTypes = new TreeSet<>();
  private final Set<String> importTypes = new TreeSet<>();
  private final List<OpenAPIResponsePrism> apiResponses;
  private final String contextPath;

  /** The producesPrism media type for the controller. Null implies JSON. */
  private final String producesPrism;

  private final boolean hasValid;
  /** Set true via {@code @Html} to indicate use of Templating */
  private final boolean html;
  private boolean methodHasValid;

  /**
   * Flag set when the controller is dependent on a request scope type.
   */
  private boolean requestScope;
  private boolean docHidden;
  private final boolean hasInstrument;

  public ControllerReader(TypeElement beanType) {
    this(beanType, "");
  }

  public ControllerReader(TypeElement beanType, String contextPath) {
    this.beanType = beanType;
    this.contextPath = contextPath;
    this.interfaces = initInterfaces(beanType);
    this.interfaceMethods = initInterfaceMethods();
    this.roles = buildRoles();
    if (isOpenApiAvailable()) {
      docHidden = initDocHidden();
    }
    this.hasValid = initHasValid();
    this.html = initHtml();
    this.producesPrism = initProduces(html);
    this.apiResponses = buildApiResponses();
    hasInstrument =
      instrumentAllWebMethods()
        || findAnnotation(InstrumentServerContextPrism::getOptionalOn)
        .map(x -> true)
        .orElse(false);
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
      if (useComponent()) {
        importTypes.add(Constants.COMPONENT);
      } else {
        importTypes.add(useJavax() ? Constants.SINGLETON_JAVAX : Constants.SINGLETON_JAKARTA);
      }
    }
  }

  private List<TypeElement> initInterfaces(TypeElement element) {
    final List<TypeElement> superInterfaces = new ArrayList<>();
    for (final TypeMirror anInterface : element.getInterfaces()) {
      final var ifaceElement = asElement(anInterface);
      final var controller = ControllerPrism.getInstanceOn(ifaceElement);
      if (controller != null && !controller.value().isBlank()
          || PathPrism.isPresent(ifaceElement)
          || ClientPrism.isPresent(ifaceElement)) {
        superInterfaces.add(ifaceElement);
      }
    }
    return superInterfaces;
  }

  private List<ExecutableElement> initInterfaceMethods() {
    final List<ExecutableElement> ifaceMethods = new ArrayList<>();
    for (final Element anInterface : interfaces) {
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
    for (final var interfaceMethod : interfaceMethods) {
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

  private boolean initHtml() {
    return findAnnotation(HtmlPrism::getOptionalOn).isPresent();
  }

  private String initProduces(boolean html) {
    String defaultProduces = html ? "text/html;charset=UTF8" : null;
    return findAnnotation(ProducesPrism::getOptionalOn).map(ProducesPrism::value).orElse(defaultProduces);
  }

  private boolean initDocHidden() {
    return findAnnotation(HiddenPrism::getOptionalOn).isPresent();
  }

  private boolean initHasValid() {
    return findAnnotation(ValidPrism::getOptionalOn).isPresent();
  }

  String produces() {
    return producesPrism;
  }

  public boolean html() {
    return html;
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
      platform().controllerRoles(roles, this);
    }
    for (final Element element : beanType.getEnclosedElements()) {
      if (element.getKind() == ElementKind.METHOD) {
        readMethod((ExecutableElement) element);
      } else if (element.getKind() == ElementKind.FIELD) {
        readField(element);
      }
    }
    readSuper(beanType);

    if (platform().getClass().getSimpleName().contains("Client")) {
      for (final var superInterface : interfaces) {
        readInterfaces(superInterface);
      }
    }
    deriveIncludeValidation();
    addImports(withSingleton);
  }

  private void deriveIncludeValidation() {
    methodHasValid = methodHasValid();
  }

  private boolean methodHasValid() {
    for (final MethodReader method : methods) {
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
    final TypeMirror superclass = beanType.getSuperclass();
    if (superclass.getKind() != TypeKind.NONE) {
      final DeclaredType declaredType = (DeclaredType) superclass;
      final TypeElement superElement = asElement(superclass);
      if (!"java.lang.Object".equals(superElement.toString())) {
        for (final Element element : superElement.getEnclosedElements()) {
          if (element.getKind() == ElementKind.METHOD) {
            readMethod((ExecutableElement) element, declaredType);
          } else if (element.getKind() == ElementKind.FIELD) {
            readField(element);
          }
        }
        readSuper(superElement);
      }
    }
  }

  /**
   * Read methods from interfaces taking into account generics.
   */
  private void readInterfaces(TypeElement interfaceElement) {
    for (final var element : ElementFilter.methodsIn(interfaceElement.getEnclosedElements())) {
      readMethod(element, (DeclaredType) interfaceElement.asType());
    }
    for (final var element : initInterfaces(interfaceElement)) {
      readInterfaces(element);
    }
  }

  private void readMethod(ExecutableElement element) {
    readMethod(element, null);
  }

  private void readMethod(ExecutableElement method, DeclaredType declaredType) {
    ExecutableType actualExecutable = null;
    if (declaredType != null) {
      // actual taking into account generics
      actualExecutable = (ExecutableType) asMemberOf(declaredType, method);
    }
    final MethodReader methodReader = new MethodReader(this, method, actualExecutable);
    if (methodReader.isWebMethod() && seenMethods.add(method.toString())) {
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
    var path =
      findAnnotation(WebAPIPrism::getOptionalOn)
        .map(WebAPIPrism::value)
        .filter(not(String::isBlank))
        .or(() -> findAnnotation(PathPrism::getOptionalOn).map(PathPrism::value))
        .map(Util::trimPath)
        .orElse(null);
    return Util.combinePath(contextPath, path);
  }

  public void addImportType(String rawType) {
    if (rawType.indexOf('.') > 0) {
      importTypes.add(sanitizeImports(rawType));
    }
  }

  public void addImportTypes(Set<String> types) {
    for (final String type : types) {
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

  public boolean hasInstrument() {
    return hasInstrument;
  }

  public static String sanitizeImports(String type) {
    final int pos = type.indexOf("@");
    if (pos == -1) {
      return trimArrayBrackets(type);
    }
    final var start = pos == 0 ? type.substring(0, pos) : "";
    return start + trimArrayBrackets(type.substring(type.lastIndexOf(' ') + 1));
  }

  private static String trimArrayBrackets(String type) {
    return type.replaceAll("[^\\n\\r\\t $;\\w.]", "");
  }
}
