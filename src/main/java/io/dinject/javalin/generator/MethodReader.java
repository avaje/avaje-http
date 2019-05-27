package io.dinject.javalin.generator;

import io.dinject.controller.Delete;
import io.dinject.controller.Get;
import io.dinject.controller.Patch;
import io.dinject.controller.Post;
import io.dinject.controller.Put;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.dinject.javalin.generator.Constants.JAVALIN_ROLES;

class MethodReader {

  private final ProcessingContext ctx;
  private final BeanReader bean;
  private final ExecutableElement element;

  private final boolean isVoid;
  private final List<MethodParam> params = new ArrayList<>();
  private final String beanPath;

  private WebMethod webMethod;
  private String webMethodPath;

  /**
   * Holds enum Roles that are required for the method.
   */
  private final List<String> methodRoles;

  MethodReader(BeanReader bean, ExecutableElement element, ProcessingContext ctx) {
    this.ctx = ctx;
    this.bean = bean;
    this.beanPath = bean.getPath();
    this.element = element;
    this.isVoid = element.getReturnType().toString().equals("void");
    this.methodRoles = Util.findRoles(element);
  }

  void read() {
    if (!methodRoles.isEmpty()) {
      bean.addStaticImportType(JAVALIN_ROLES);
      for (String role : methodRoles) {
        bean.addStaticImportType(role);
      }
    }

    for (VariableElement p : element.getParameters()) {
      MethodParam param = new MethodParam(p, ctx);
      params.add(param);
      param.addImports(bean);
    }
  }

  void addRoute(Append writer) {

    readMethodAnnotation();

    if (webMethod != null) {

      String fullPath = Util.combinePath(beanPath, webMethodPath);
      Set<String> pathParams = Util.pathParams(fullPath);

      writer.append("    ApiBuilder.%s(\"%s\", ctx -> {", webMethod.name().toLowerCase(), fullPath).eol();
      writer.append("      ctx.status(%s);", httpStatusCode()).eol();
      for (MethodParam param : params) {
        param.buildCtxGet(writer, pathParams);
      }
      writer.append("      ");

      if (isReturnJson()) {
        writer.append("ctx.json(");
      }
      writer.append("controller.");
      writer.append(element.getSimpleName().toString()).append("(");
      for (int i = 0; i < params.size(); i++) {
        if (i > 0) {
          writer.append(", ");
        }
        params.get(i).buildParamName(writer);
      }
      writer.append(")");
      if (isReturnJson()) {
        writer.append(")");
      }
      writer.append(";").eol();
      writer.append("    }");

      List<String> roles = roles();
      if (!roles.isEmpty()) {
        writer.append(", roles(");
        for (int i = 0; i < roles.size(); i++) {
          if (i > 0) {
            writer.append(", ");
          }
          writer.append(Util.shortName(roles.get(i)));
        }
        writer.append(")");
      }
      writer.append(");");
      writer.eol().eol();
    }
  }

  private List<String> roles() {
    return methodRoles.isEmpty() ? bean.getRoles() : methodRoles;
  }

  private int httpStatusCode() {
    return webMethod.statusCode();
  }

  private boolean isReturnJson() {
    // TODO: ... returning non-object types?
    return !isVoid;
  }


  private boolean readMethodAnnotation() {

    Get get = element.getAnnotation(Get.class);
    if (get != null) {
      return setWebMethod(WebMethod.GET, get.value());
    }

    Put put = element.getAnnotation(Put.class);
    if (put != null) {
      return setWebMethod(WebMethod.PUT, put.value());
    }

    Post post = element.getAnnotation(Post.class);
    if (post != null) {
      return setWebMethod(WebMethod.POST, post.value());
    }
    Patch patch = element.getAnnotation(Patch.class);
    if (patch != null) {
      return setWebMethod(WebMethod.PATCH, patch.value());
    }
    Delete delete = element.getAnnotation(Delete.class);
    if (delete != null) {
      return setWebMethod(WebMethod.DELETE, delete.value());
    }

    return false;
  }

  private boolean setWebMethod(WebMethod webMethod, String value) {
    this.webMethod = webMethod;
    this.webMethodPath = value;
    return true;
  }
}
