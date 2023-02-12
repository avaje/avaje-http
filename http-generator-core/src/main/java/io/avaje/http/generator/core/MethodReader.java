package io.avaje.http.generator.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import io.avaje.http.generator.core.javadoc.Javadoc;
import io.avaje.http.generator.core.openapi.MethodDocBuilder;

public class MethodReader {

  private final ProcessingContext ctx;
  private final ControllerReader bean;
  private final ExecutableElement element;
  private final boolean isVoid;
  private final List<MethodParam> params = new ArrayList<>();
  private final Javadoc javadoc;
  /**
   * Holds enum Roles that are required for the method.
   */
  private final List<String> methodRoles;
  private final Optional<ProducesPrism> producesAnnotation;
  private final List<OpenAPIResponsePrism> apiResponses;
  private final ExecutableType actualExecutable;
  private final List<? extends TypeMirror> actualParams;
  private final PathSegments pathSegments;
  private final boolean hasValid;
  private final List<ExecutableElement> superMethods;

  private WebMethod webMethod;
  private String webMethodPath;
  private boolean formMarker;

  MethodReader(ControllerReader bean, ExecutableElement element, ExecutableType actualExecutable, ProcessingContext ctx) {
    this.ctx = ctx;
    this.bean = bean;
    this.element = element;
    this.actualExecutable = actualExecutable;
    this.actualParams = (actualExecutable == null) ? null : actualExecutable.getParameterTypes();
    this.isVoid = element.getReturnType().getKind() == TypeKind.VOID;
    this.methodRoles = Util.findRoles(element);
    this.producesAnnotation = findAnnotation(ProducesPrism::getOptionalOn);
    initWebMethodViaAnnotation();

    this.superMethods =
        ctx.superMethods(element.getEnclosingElement(), element.getSimpleName().toString());
    superMethods.forEach(m -> methodRoles.addAll(Util.findRoles(m)));

    this.apiResponses = buildApiResponses();
    this.javadoc = buildJavadoc(element, ctx);

    if (isWebMethod()) {
      this.hasValid = initValid();
      this.pathSegments = PathSegments.parse(Util.combinePath(bean.path(), webMethodPath));
    } else {
      this.hasValid = false;
      this.pathSegments = null;
    }
  }

  private Javadoc buildJavadoc(ExecutableElement element, ProcessingContext ctx) {
    return Optional.of(Javadoc.parse(ctx.docComment(element)))
        .filter(Predicate.not(Javadoc::isEmpty))
        .orElseGet(
            () ->
                superMethods.stream()
                    .map(e -> Javadoc.parse(ctx.docComment(e)))
                    .filter(Predicate.not(Javadoc::isEmpty))
                    .findFirst()
                    .orElse(Javadoc.parse("")));
  }

  private boolean initValid() {
    return findAnnotation(ValidPrism::getOptionalOn).isPresent()
        || findAnnotation(JavaxValidPrism::getOptionalOn).isPresent()
        || superMethodHasValid();
  }

  private boolean superMethodHasValid() {
    return superMethods.stream()
        .anyMatch(
            e ->
                findAnnotation(ValidPrism::getOptionalOn).isPresent()
                    || findAnnotation(JavaxValidPrism::getOptionalOn).isPresent());
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
        .ifPresent(get -> initSetWebMethod(WebMethod.GET, get.value()));

    findAnnotation(PutPrism::getOptionalOn)
        .ifPresent(put -> initSetWebMethod(WebMethod.PUT, put.value()));

    findAnnotation(PostPrism::getOptionalOn)
        .ifPresent(post -> initSetWebMethod(WebMethod.POST, post.value()));

    findAnnotation(PatchPrism::getOptionalOn)
        .ifPresent(patch -> initSetWebMethod(WebMethod.PATCH, patch.value()));
    findAnnotation(DeletePrism::getOptionalOn)
        .ifPresent(delete -> initSetWebMethod(WebMethod.DELETE, delete.value()));
  }

  private void initSetWebMethod(WebMethod webMethod, String value) {
    this.webMethod = webMethod;
    this.webMethodPath = value;
  }

  public Javadoc javadoc() {
    return javadoc;
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

    final var responses =
        Stream.concat(methodResponses, superMethodResponses).collect(Collectors.toList());

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
      ctx.platform().methodRoles(methodRoles, bean);
    }
    // non-path parameters default to form or query parameters based on the
    // existence of @Form annotation on the method
    ParamType defaultParamType = (formMarker) ? ParamType.FORMPARAM : ParamType.QUERYPARAM;

    final List<? extends VariableElement> parameters = element.getParameters();
    for (int i = 0; i < parameters.size(); i++) {
      VariableElement p = parameters.get(i);
      TypeMirror typeMirror;
      if (actualParams != null) {
        typeMirror = actualParams.get(i);
      } else {
        typeMirror = p.asType();
      }
      String rawType = Util.typeDef(typeMirror);
      UType type = Util.parse(typeMirror.toString());
      MethodParam param = new MethodParam(p, type, rawType, ctx, defaultParamType, formMarker);
      params.add(param);
      param.addImports(bean);
    }
  }

  public void buildApiDoc() {
    buildApiDocumentation(ctx);
  }

  /**
   * Build the OpenAPI documentation for the method / operation.
   */
  public void buildApiDocumentation(ProcessingContext ctx) {
    new MethodDocBuilder(this, ctx.doc()).build();
  }

  public List<String> roles() {
    var roles = new ArrayList<>(methodRoles);
    roles.addAll(bean.roles());
    return roles;
  }

  public boolean isWebMethod() {
    return webMethod != null;
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
    return producesAnnotation.map(ProducesPrism::defaultStatus).filter(s -> s > 0).isPresent();
  }

  public String produces() {
    return producesAnnotation.map(ProducesPrism::value).orElseGet(bean::produces);
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

  public String statusCode() {
    return producesAnnotation
        .map(ProducesPrism::defaultStatus)
        .filter(s -> s > 0)
        .orElseGet(() -> webMethod.statusCode(isVoid))
        .toString();
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
    for (MethodParam param : params) {
      if (param.isForm()) {
        return true;
      }
    }
    return false;
  }

  public String bodyType() {
    for (MethodParam param : params) {
      if (param.isBody()) {
        return param.shortType();
      }
    }
    return null;
  }

  public String bodyName() {
    for (MethodParam param : params) {
      if (param.isBody()) {
        return param.name();
      }
    }
    return "body";
  }
}
