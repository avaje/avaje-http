package io.dinject.webroutegen;

import io.dinject.controller.Delete;
import io.dinject.controller.Form;
import io.dinject.controller.Get;
import io.dinject.controller.Patch;
import io.dinject.controller.Post;
import io.dinject.controller.Produces;
import io.dinject.controller.Put;
import io.dinject.webroutegen.javadoc.Javadoc;
import io.dinject.webroutegen.openapi.MethodDocBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class MethodReader {

  private final ProcessingContext ctx;
  private final ControllerReader bean;
  private final ExecutableElement element;

  private final boolean isVoid;
  private final List<MethodParam> params = new ArrayList<>();

  private final Javadoc javadoc;

  private WebMethod webMethod;
  private String webMethodPath;

  private boolean formMarker;

  /**
   * Holds enum Roles that are required for the method.
   */
  private final List<String> methodRoles;

  private final String produces;

  private final ExecutableType actualExecutable;
  private final List<? extends TypeMirror> actualParams;

  private final PathSegments pathSegments;

  MethodReader(ControllerReader bean, ExecutableElement element, ExecutableType actualExecutable, ProcessingContext ctx) {
    this.ctx = ctx;
    this.bean = bean;
    this.element = element;
    this.actualExecutable = actualExecutable;
    this.actualParams = (actualExecutable == null) ? null : actualExecutable.getParameterTypes();
    this.isVoid = element.getReturnType().getKind() == TypeKind.VOID;
    this.methodRoles = Util.findRoles(element);
    this.javadoc = Javadoc.parse(ctx.getDocComment(element));
    this.produces = produces(bean);

    initWebMethodViaAnnotation();
    this.pathSegments = PathSegments.parse(Util.combinePath(bean.getPath(), webMethodPath));
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

  boolean isWebMethod() {
    return webMethod != null;
  }

  public Javadoc getJavadoc() {
    return javadoc;
  }

  private String produces(ControllerReader bean) {
    final Produces produces = findAnnotation(Produces.class);
    return (produces != null) ? produces.value() : bean.getProduces();
  }

  public <A extends Annotation> A findAnnotation(Class<A> type) {
    A annotation = element.getAnnotation(type);
    if (annotation != null) {
      return annotation;
    }

    return bean.findMethodAnnotation(type, element);
  }

  private List<String> addTagsToList(Element element, List<String> list) {
    if (element == null)
      return list;

    if (element.getAnnotation(Tag.class) != null) {
      list.add(element.getAnnotation(Tag.class).name());
    }
    if (element.getAnnotation(Tags.class) != null) {
      for (Tag tag : element.getAnnotation(Tags.class).value())
        list.add(tag.name());
    }
    return list;
  }

  public List<String> getTags() {
    List<String> tags = new ArrayList<>();
    tags = addTagsToList(element, tags);
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

      String rawType;
      if (actualParams != null) {
        rawType = Util.typeDef(actualParams.get(i));
      } else {
        rawType = Util.typeDef(p.asType());
      }

      MethodParam param = new MethodParam(p, rawType, ctx, defaultParamType, formMarker);
      params.add(param);
      param.addImports(bean);
    }
  }

  /**
   * Build the OpenAPI documentation for the method / operation.
   */
  void buildApiDocumentation(ProcessingContext ctx) {
    new MethodDocBuilder(this, ctx.doc()).build();
  }

  public List<String> roles() {
    return methodRoles.isEmpty() ? bean.getRoles() : methodRoles;
  }

  public WebMethod getWebMethod() {
    return webMethod;
  }

  public List<MethodParam> getParams() {
    return params;
  }

  public boolean isVoid() {
    return isVoid;
  }

  public String getProduces() {
    return produces;
  }

  public TypeMirror getReturnType() {
    if (actualExecutable != null) {
      return actualExecutable.getReturnType();
    }
    return element.getReturnType();
  }

  public String getStatusCode() {
    return Integer.toString(webMethod.statusCode(isVoid));
  }

  public PathSegments getPathSegments() {
    return pathSegments;
  }

  public String getFullPath() {
    return pathSegments.fullPath();
  }

  public boolean includeValidate() {
    return bean.isIncludeValidator() && webMethod != WebMethod.GET;
  }

  public String simpleName() {
    return element.getSimpleName().toString();
  }
}
