package io.dinject.javalin.generator;

import io.dinject.controller.BeanParam;
import io.dinject.controller.Cookie;
import io.dinject.controller.Default;
import io.dinject.controller.Form;
import io.dinject.controller.FormParam;
import io.dinject.controller.Header;
import io.dinject.controller.QueryParam;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.Set;

class ElementReader {

  private final ProcessingContext ctx;
  private final String rawType;
  private final TypeHandler typeHandler;
  private final String varName;
  private final String snakeName;

  private String paramName;
  private ParamType paramType;
  private String paramDefault;
  private String docComment;

  ElementReader(Element element, ProcessingContext ctx, ParamType defaultType) {
    this.ctx = ctx;
    this.rawType = element.asType().toString();
    this.typeHandler = TypeMap.get(rawType);

    this.varName = element.getSimpleName().toString();
    this.snakeName = Util.snakeCase(varName);
    this.paramName = varName;
    this.docComment = ctx.docComment(element);

    readAnnotations(element, defaultType);
  }

  private void readAnnotations(Element element, ParamType defaultType) {
    Default defaultVal = element.getAnnotation(Default.class);
    if (defaultVal != null) {
      this.paramDefault = defaultVal.value();
    }
    Form form = element.getAnnotation(Form.class);
    if (form != null) {
      this.paramType = ParamType.FORM;
      return;
    }
    BeanParam beanParam = element.getAnnotation(BeanParam.class);
    if (beanParam != null) {
      this.paramType = ParamType.BEANPARAM;
      return;
    }
    QueryParam queryParam = element.getAnnotation(QueryParam.class);
    if (queryParam != null) {
      this.paramName = nameFrom(queryParam.value(), varName);
      this.paramType = ParamType.QUERYPARAM;
      return;
    }
    FormParam formParam = element.getAnnotation(FormParam.class);
    if (formParam != null) {
      this.paramName = nameFrom(formParam.value(), varName);
      this.paramType = ParamType.FORMPARAM;
      return;
    }
    Cookie cookieParam = element.getAnnotation(Cookie.class);
    if (cookieParam != null) {
      this.paramName = nameFrom(cookieParam.value(), varName);
      this.paramType = ParamType.COOKIE;
      this.paramDefault = null;
      return;
    }
    Header headerParam = element.getAnnotation(Header.class);
    if (headerParam != null) {
      this.paramName = nameFrom(headerParam.value(), Util.initcapSnake(snakeName));
      this.paramType = ParamType.HEADER;
      this.paramDefault = null;
      return;
    }
    if (paramType == null) {
      this.paramType = defaultType;
    }
  }

  @Override
  public String toString() {
    return varName + " type:" + rawType + " paramType:" + paramType + " dft:" + paramDefault;
  }

  private String nameFrom(String name, String defaultName) {
    if (name != null && !name.isEmpty()) {
      return name;
    }
    return defaultName;
  }

  String getVarName() {
    return varName;
  }

  private boolean hasParamDefault() {
    return paramDefault != null && !paramDefault.isEmpty();
  }

  private boolean isJavalinContext() {
    return Constants.JAVALIN_CONTEXT.equals(rawType);
  }

  private String shortType() {
    if (typeHandler != null) {
      return typeHandler.shortName();
    } else {
      return Util.shortName(rawType);
    }
  }

  private String derivePathParam(Set<String> pathParams) {
    if (pathParams.contains(varName)) {
      return varName;
    }
    if (pathParams.contains(snakeName)) {
      return snakeName;
    }
    return null;
  }

  void addImports(ControllerReader bean) {
    if (typeHandler != null) {
      String importType = typeHandler.getImportType();
      if (importType != null) {
        bean.addImportType(rawType);
      }
    } else {
      bean.addImportType(rawType);
    }
  }

  void writeParamName(Append writer) {
    if (isJavalinContext()) {
      writer.append("ctx");
    } else {
      writer.append(varName);
    }
  }

  void writeCtxGet(Append writer, Set<String> pathParams) {

    if (isJavalinContext()) {
      // no conversion for this parameter
      return;
    }

    String shortType = shortType();
    writer.append("      %s %s = ", shortType, varName);
    if (setValue(writer, pathParams, shortType)) {
      writer.append(";").eol();
    }
  }

  void setValue(Append writer) {
    String shortType = shortType();
    setValue(writer, Collections.emptySet(), shortType);
  }

  private boolean setValue(Append writer, Set<String> pathParams, String shortType) {

    if (ParamType.FORM == paramType) {
      writeForm(writer, shortType, varName, ParamType.FORMPARAM);
      return false;
    }
    if (ParamType.BEANPARAM == paramType) {
      writeForm(writer, shortType, varName, ParamType.QUERYPARAM);
      return false;
    }

    // path parameters are expected to be not nullable
    // ... with query parameters nullable
    String pathParameter = derivePathParam(pathParams);
    String asMethod = (typeHandler == null) ? null : (pathParameter != null) ? typeHandler.asMethod() : typeHandler.toMethod();

    if (asMethod != null) {
      writer.append(asMethod);
    }
    if (pathParameter != null) {
      writer.append("ctx.pathParam(\"%s\")", pathParameter);
    } else {
      if (typeHandler == null) {
        // assuming this is a body (POST, PATCH)
        writer.append("ctx.bodyAsClass(%s.class)", shortType);
      } else {
        if (hasParamDefault()) {
          writer.append("ctx.%s(\"%s\",\"%s\")", paramType, paramName, paramDefault);
        } else {
          writer.append("ctx.%s(\"%s\")", paramType, paramName);
        }
      }
    }

    if (asMethod != null) {
      writer.append(")");
    }
    return true;
  }

  private void writeForm(Append writer, String shortType, String varName, ParamType defaultParamType) {

    TypeElement formBeanType = ctx.getTypeElement(rawType);

    BeanParamReader form = new BeanParamReader(ctx, formBeanType, varName, shortType, defaultParamType);
    form.write(writer);
  }
}
