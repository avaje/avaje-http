package io.avaje.http.generator.core;

import io.avaje.http.api.*;
import io.avaje.http.generator.core.javadoc.Javadoc;
import io.avaje.http.generator.core.openapi.MethodDocBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.validation.Valid;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  private final Optional<Produces> producesAnnotation;
  private final List<OpenAPIResponse> apiResponses;
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
    this.producesAnnotation = Optional.ofNullable(findAnnotation(Produces.class));
    initWebMethodViaAnnotation();

    this.superMethods = ctx.superMethods(element.getEnclosingElement(), element.getSimpleName().toString());
    superMethods.forEach(m -> methodRoles.addAll(Util.findRoles(m)));

    this.apiResponses = buildApiResponses();
    this.javadoc = buildJavadoc(element, ctx);

    if (isWebMethod()) {
      Class<Annotation> jakartaValidAnnotation;
      try {
        jakartaValidAnnotation = jakartaValidAnnotation();
      } catch (final ClassNotFoundException e) {
        jakartaValidAnnotation = null;
      }
      this.hasValid = hasValid(jakartaValidAnnotation);
      this.pathSegments = PathSegments.parse(Util.combinePath(bean.path(), webMethodPath));
    } else {
      this.hasValid = false;
      this.pathSegments = null;
    }
  }

  private Javadoc buildJavadoc(ExecutableElement element, ProcessingContext ctx) {
    return Optional.of(Javadoc.parse(ctx.docComment(element)))
      .filter(Predicate.not(Javadoc::isEmpty))
      .orElseGet(() -> superMethods.stream()
        .map(e -> Javadoc.parse(ctx.docComment(e)))
        .filter(Predicate.not(Javadoc::isEmpty))
        .findFirst()
        .orElse(Javadoc.parse("")));
  }

  private boolean hasValid(Class<Annotation> jakartaValidAnnotation) {
    return findAnnotation(Valid.class) != null
      || (jakartaValidAnnotation != null && findAnnotation(jakartaValidAnnotation) != null)
      || superMethodHasValid(jakartaValidAnnotation);
  }

  private boolean superMethodHasValid(Class<Annotation> jakartaAnnotation) {
    return superMethods.stream()
      .anyMatch(e ->
        findAnnotation(Valid.class, e) != null
          || (jakartaAnnotation != null && findAnnotation(jakartaAnnotation, e) != null));
  }

  @SuppressWarnings("unchecked")
  private static Class<Annotation> jakartaValidAnnotation() throws ClassNotFoundException {
    return (Class<Annotation>) Class.forName(Valid.class.getCanonicalName().replace("javax", "jakarta"));
  }

  @Override
  public String toString() {
    return element.toString();
  }

  private void initWebMethodViaAnnotation() {
    Form form = findAnnotation(Form.class);
    if (form != null) {
      this.formMarker = true;
    }
    Get get = findAnnotation(Get.class);
    if (get != null) {
      initSetWebMethod(WebMethod.GET, get.value());
      return;
    }
    Put put = findAnnotation(Put.class);
    if (put != null) {
      initSetWebMethod(WebMethod.PUT, put.value());
      return;
    }
    Post post = findAnnotation(Post.class);
    if (post != null) {
      initSetWebMethod(WebMethod.POST, post.value());
      return;
    }
    Patch patch = findAnnotation(Patch.class);
    if (patch != null) {
      initSetWebMethod(WebMethod.PATCH, patch.value());
      return;
    }
    Delete delete = findAnnotation(Delete.class);
    if (delete != null) {
      initSetWebMethod(WebMethod.DELETE, delete.value());
    }
  }

  private void initSetWebMethod(WebMethod webMethod, String value) {
    this.webMethod = webMethod;
    this.webMethodPath = value;
  }

  public Javadoc javadoc() {
    return javadoc;
  }

  private List<OpenAPIResponse> buildApiResponses() {
    final var container =
      Optional.ofNullable(findAnnotation(OpenAPIResponses.class)).stream()
        .map(OpenAPIResponses::value)
        .flatMap(Arrays::stream);

    final var methodResponses =
      Stream.concat(
        container, Arrays.stream(element.getAnnotationsByType(OpenAPIResponse.class)));

    final var superMethodResponses =
      superMethods.stream()
        .flatMap(
          method ->
            Stream.concat(
              Optional.ofNullable(findAnnotation(OpenAPIResponses.class, method)).stream()
                .map(OpenAPIResponses::value)
                .flatMap(Arrays::stream),
              Arrays.stream(method.getAnnotationsByType(OpenAPIResponse.class))));

    return Stream.concat(methodResponses, superMethodResponses).collect(Collectors.toList());
  }

  public <A extends Annotation> A findAnnotation(Class<A> type) {
    return findAnnotation(type, element);
  }

  public <A extends Annotation> A findAnnotation(Class<A> type, ExecutableElement elem) {
    final var annotation = elem.getAnnotation(type);
    if (annotation != null) {
      return annotation;
    }
    return bean.findMethodAnnotation(type, elem);
  }

  private List<String> addTagsToList(Element element, List<String> list) {
    if (element == null) {
      return list;
    }
    if (element.getAnnotation(Tag.class) != null) {
      list.add(element.getAnnotation(Tag.class).name());
    }
    if (element.getAnnotation(Tags.class) != null) {
      for (Tag tag : element.getAnnotation(Tags.class).value()) {
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
    return producesAnnotation.map(Produces::defaultStatus).filter(s -> s > 0).isPresent();
  }

  public String produces() {
    return producesAnnotation.map(Produces::value).orElseGet(bean::produces);
  }

  public List<OpenAPIResponse> apiResponses() {
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
        .map(Produces::defaultStatus)
        .filter(s -> s > 0)
        .orElseGet(() -> webMethod.statusCode(isVoid)).toString();
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
