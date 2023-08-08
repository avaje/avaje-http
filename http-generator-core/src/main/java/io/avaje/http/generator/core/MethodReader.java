package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ProcessingContext.*;
import static io.avaje.http.generator.core.ProcessingContext.docComment;
import static io.avaje.http.generator.core.ProcessingContext.platform;
import static io.avaje.http.generator.core.ProcessingContext.superMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import io.avaje.http.generator.core.javadoc.Javadoc;
import io.avaje.http.generator.core.openapi.MethodDocBuilder;

public class MethodReader {

  private final ControllerReader bean;
  private final ExecutableElement element;
  private final boolean isVoid;
  private final List<MethodParam> params = new ArrayList<>();
  private final Javadoc javadoc;
  /** Holds enum Roles that are required for the method. */
  private final List<String> methodRoles;

  private final Optional<ProducesPrism> producesAnnotation;
  private final Optional<ConsumesPrism> consumesAnnotation;
  private final List<SecurityRequirementPrism> securityRequirements;
  private final List<OpenAPIResponsePrism> apiResponses;
  private final ExecutableType actualExecutable;
  private final List<? extends TypeMirror> actualParams;
  private final PathSegments pathSegments;
  private final boolean hasValid;
  private final List<ExecutableElement> superMethods;
  private final Optional<RequestTimeoutPrism> timeout;

  private WebMethod webMethod;
  private int statusCode;
  private String webMethodPath;
  private boolean formMarker;
  private final boolean instrumentContext;
  private final boolean hasThrows;
  private String exceptionShortName;

  MethodReader(ControllerReader bean, ExecutableElement element, ExecutableType actualExecutable) {
    this.bean = bean;
    this.element = element;
    this.actualExecutable = actualExecutable;
    this.actualParams = (actualExecutable == null) ? null : actualExecutable.getParameterTypes();
    this.isVoid = element.getReturnType().getKind() == TypeKind.VOID;
    this.methodRoles = Util.findRoles(element);
    this.producesAnnotation =
        findAnnotation(ProducesPrism::getOptionalOn)
            .or(() -> ProducesPrism.getOptionalOn(bean.beanType()));
    this.consumesAnnotation =
        findAnnotation(ConsumesPrism::getOptionalOn)
            .or(() -> ConsumesPrism.getOptionalOn(bean.beanType()));

    initWebMethodViaAnnotation();

    this.superMethods =
        superMethods(element.getEnclosingElement(), element.getSimpleName().toString());
    superMethods.forEach(m -> methodRoles.addAll(Util.findRoles(m)));
    this.hasThrows = !element.getThrownTypes().isEmpty();
    this.securityRequirements = readSecurityRequirements();
    this.apiResponses = buildApiResponses();
    this.javadoc = buildJavadoc(element);
    this.timeout = RequestTimeoutPrism.getOptionalOn(element);
    timeout.ifPresent(
        p -> {
          bean.addStaticImportType("java.time.temporal.ChronoUnit." + p.chronoUnit());
          bean.addStaticImportType("java.time.Duration.of");
        });
    if (isWebMethod()) {
      this.hasValid = initValid();
      this.instrumentContext = initResolver();
      this.pathSegments = PathSegments.parse(Util.combinePath(bean.path(), webMethodPath));
    } else {
      this.hasValid = false;
      this.pathSegments = null;
      this.instrumentContext = false;
    }
  }

  private boolean initResolver() {
    return bean.hasInstrument()
        || hasInstrument(element)
        || superMethods.stream().anyMatch(this::hasInstrument);
  }

  private boolean hasInstrument(Element e) {
    return InstrumentServerContextPrism.getOptionalOn(e).isPresent();
  }

  private Javadoc buildJavadoc(ExecutableElement element) {
    return Optional.of(Javadoc.parse(docComment(element)))
        .filter(Predicate.not(Javadoc::isEmpty))
        .orElseGet(
            () ->
                superMethods.stream()
                    .map(e -> Javadoc.parse(docComment(e)))
                    .filter(Predicate.not(Javadoc::isEmpty))
                    .findFirst()
                    .orElse(Javadoc.parse("")));
  }

  private boolean initValid() {
    return findAnnotation(ValidPrism::getOptionalOn).isPresent() || superMethodHasValid();
  }

  private boolean superMethodHasValid() {
    return superMethods.stream()
        .anyMatch(e -> findAnnotation(ValidPrism::getOptionalOn).isPresent());
  }

  @Override
  public String toString() {
    return element.toString();
  }

  private void initWebMethodViaAnnotation() {
    if (findAnnotation(FormPrism::getOptionalOn).isPresent()) {
      this.formMarker = true;
    }

    findAnnotation(GetPrism::getOptionalOn)
        .ifPresent(get -> initSetWebMethod(CoreWebMethod.GET, get.value()));

    findAnnotation(PutPrism::getOptionalOn)
        .ifPresent(put -> initSetWebMethod(CoreWebMethod.PUT, put.value()));

    findAnnotation(PostPrism::getOptionalOn)
        .ifPresent(post -> initSetWebMethod(CoreWebMethod.POST, post.value()));

    findAnnotation(PatchPrism::getOptionalOn)
        .ifPresent(patch -> initSetWebMethod(CoreWebMethod.PATCH, patch.value()));

    findAnnotation(DeletePrism::getOptionalOn)
        .ifPresent(delete -> initSetWebMethod(CoreWebMethod.DELETE, delete.value()));

    findAnnotation(ExceptionHandlerPrism::getOptionalOn)
    .ifPresent(error -> initSetWebMethod(CoreWebMethod.ERROR, error));

    findAnnotation(FilterPrism::getOptionalOn)
        .ifPresent(filter -> initSetWebMethod(CoreWebMethod.FILTER, ""));

    platform()
        .customHandlers()
        .forEach(f -> findAnnotation(f).ifPresent(m -> initSetWebMethod(m.webMethod(), m.value())));
  }

  private void initSetWebMethod(WebMethod webMethod, String value) {
    this.webMethod = webMethod;
    this.webMethodPath = value;
  }

  private void initSetWebMethod(WebMethod webMethod, ExceptionHandlerPrism exceptionPrism) {
    this.webMethod = webMethod;
    this.statusCode = exceptionPrism.statusCode();
    var exType = exceptionPrism.value().toString();
    if ("io.avaje.http.api.DefaultException".equals(exType)) {
      exType =
          element.getParameters().stream()
              .map(VariableElement::asType)
              .map(UType::parse)
              .map(UType::mainType)
              .filter(t -> isAssignable2Interface(t, "java.lang.Throwable"))
              .findAny()
              .orElseGet(
                  () -> {
                    logError(
                        element,
                        "Must define a parameter that extends Throwable or define the exception type in the ExceptionHandler annotation");
                    return "";
                  });
    }
    this.exceptionShortName = Util.shortName(exType);
    bean.addImportType(exType);
  }

  public Javadoc javadoc() {
    return javadoc;
  }

  private List<SecurityRequirementPrism> readSecurityRequirements() {
    final var list = new ArrayList<SecurityRequirementPrism>();

    readSecurityRequirements(element, list);
    for (final ExecutableElement superMethod : superMethods) {
      readSecurityRequirements(superMethod, list);
    }
    readSecurityRequirements(bean.beanType(), list);

    final var map = new HashMap<String, SecurityRequirementPrism>();
    for (final SecurityRequirementPrism p : list) {
      if (!map.containsKey(p.name())) {
        map.put(p.name(), p);
      }
    }
    return List.copyOf(map.values());
  }

  private void readSecurityRequirements(Element element, List<SecurityRequirementPrism> list) {
    final Consumer<Element> f =
        e -> {
          Optional.ofNullable(SecurityRequirementsPrism.getInstanceOn(e))
              .map(SecurityRequirementsPrism::value)
              .ifPresent(list::addAll);
          Optional.ofNullable(SecurityRequirementPrism.getAllInstancesOn(e))
              .ifPresent(list::addAll);
        };
    f.accept(element);

    for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      // find only one level
      f.accept(annotationMirror.getAnnotationType().asElement());
    }
  }

  private List<OpenAPIResponsePrism> buildApiResponses() {
    final var container =
        findAnnotation(OpenAPIResponsesPrism::getOptionalOn).stream()
            .map(OpenAPIResponsesPrism::value)
            .flatMap(List::stream);

    final var methodResponses =
        Stream.concat(container, OpenAPIResponsePrism.getAllInstancesOn(element).stream());

    final var superMethodResponses =
        superMethods.stream()
            .flatMap(
                method ->
                    Stream.concat(
                        findAnnotation(OpenAPIResponsesPrism::getOptionalOn, method).stream()
                            .map(OpenAPIResponsesPrism::value)
                            .flatMap(List::stream),
                        OpenAPIResponsePrism.getAllInstancesOn(method).stream()));

    final var responses = Stream.concat(methodResponses, superMethodResponses).collect(Collectors.toList());
    responses.addAll(bean.openApiResponses());
    return responses;
  }

  public <A> Optional<A> findAnnotation(Function<Element, Optional<A>> prismFunc) {
    return findAnnotation(prismFunc, element);
  }

  public <A> Optional<A> findAnnotation(Function<Element, Optional<A>> prismFunc, ExecutableElement elem) {
    return prismFunc.apply(elem).or(() -> bean.findMethodAnnotation(prismFunc, elem));
  }

  private List<String> addTagsToList(Element element, List<String> list) {
    if (element == null) {
      return list;
    }

    TagPrism.getAllInstancesOn(element).forEach(t -> list.add(t.name()));
    final var tags = TagsPrism.getInstanceOn(element);
    if (tags != null) {
      for (final var tag : tags.value()) {
        list.add(tag.name());
      }
    }
    return list;
  }

  public List<String> tags() {
    final var tags = addTagsToList(element, new ArrayList<>());
    superMethods.forEach(method -> addTagsToList(method, tags));
    return addTagsToList(element.getEnclosingElement(), tags);
  }

  void read() {
    if (!methodRoles.isEmpty()) {
      platform().methodRoles(methodRoles, bean);
    }
    // non-path parameters default to form or query parameters based on the
    // existence of @Form annotation on the method
    final ParamType defaultParamType = (formMarker) ? ParamType.FORMPARAM : ParamType.QUERYPARAM;

    final List<? extends VariableElement> parameters = element.getParameters();
    for (int i = 0; i < parameters.size(); i++) {
      final VariableElement p = parameters.get(i);
      TypeMirror typeMirror;
      if (actualParams != null) {
        typeMirror = actualParams.get(i);
      } else {
        typeMirror = p.asType();
      }
      final String rawType = Util.typeDef(typeMirror);
      final UType type = Util.parse(typeMirror.toString());
      final MethodParam param = new MethodParam(p, type, rawType, defaultParamType, formMarker);
      params.add(param);
      param.addImports(bean);
    }
  }

  public void buildApiDoc() {
    buildApiDocumentation();
  }

  /** Build the OpenAPI documentation for the method / operation. */
  public void buildApiDocumentation() {
    if (!isErrorMethod()
        && webMethod instanceof CoreWebMethod
        && webMethod != CoreWebMethod.FILTER) {
      new MethodDocBuilder(this, doc()).build();
    }
  }

  public List<String> roles() {
    final var roles = new ArrayList<>(methodRoles);
    roles.addAll(bean.roles());
    return roles;
  }

  public boolean isWebMethod() {
    return webMethod != null;
  }

  public boolean isErrorMethod() {
    return webMethod == CoreWebMethod.ERROR;
  }

  public WebMethod webMethod() {
    return webMethod;
  }

  public String webMethodPath() {
    return webMethodPath;
  }

  public List<MethodParam> params() {
    return params;
  }

  public boolean isVoid() {
    return isVoid;
  }

  public boolean hasProducesStatus() {
    return producesAnnotation.map(ProducesPrism::statusCode).filter(s -> s > 0).isPresent();
  }

  public String produces() {
    return producesAnnotation.map(ProducesPrism::value).orElseGet(bean::produces);
  }

  public Optional<ConsumesPrism> consumesAnnotation() {
    return consumesAnnotation;
  }

  public List<SecurityRequirementPrism> securityRequirements() {
    return securityRequirements;
  }

  public List<OpenAPIResponsePrism> apiResponses() {
    return apiResponses;
  }

  public TypeMirror returnType() {
    if (actualExecutable != null) {
      return actualExecutable.getReturnType();
    }
    return element.getReturnType();
  }

  public int statusCode() {
    if (statusCode != 0) {
      // using explicit status code
      return statusCode;
    }
    return producesAnnotation
        .map(ProducesPrism::statusCode)
        .filter(s -> s != 0)
        .orElseGet(() -> webMethod.statusCode(isVoid));
  }

  public PathSegments pathSegments() {
    return pathSegments;
  }

  public String fullPath() {
    return pathSegments.fullPath();
  }

  public boolean includeValidate() {
    return bean.hasValid() || hasValid;
  }

  boolean hasValid() {
    return hasValid;
  }

  public String simpleName() {
    return element.getSimpleName().toString();
  }

  public boolean isFormBody() {
    for (final MethodParam param : params) {
      if (param.isForm()) {
        return true;
      }
    }
    return false;
  }

  public String bodyType() {
    for (final MethodParam param : params) {
      if (param.isBody()) {
        return param.shortType();
      }
    }
    return null;
  }

  public String bodyName() {
    for (final MethodParam param : params) {
      if (param.isBody()) {
        return param.name();
      }
    }
    return "body";
  }

  public Optional<RequestTimeoutPrism> timeout() {
    return timeout;
  }

  public boolean instrumentContext() {
    return instrumentContext;
  }

  public void writeContext(Append writer, String reqName, String resName) {
    if (isVoid) {
      writer.append("resolver.runWith");
    } else if (hasThrows) {
      writer.append("resolver.callWith");
    } else {
      writer.append("resolver.supplyWith");
    }
    writer.append("(new ServerContext(%s, %s), () -> ", reqName, resName);
  }

  public ExecutableElement element() {
    return element;
  }

  public String exceptionShortName() {
    return exceptionShortName;
  }
}
