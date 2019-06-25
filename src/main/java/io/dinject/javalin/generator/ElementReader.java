package io.dinject.javalin.generator;

import io.dinject.controller.BeanParam;
import io.dinject.controller.Cookie;
import io.dinject.controller.Default;
import io.dinject.controller.Form;
import io.dinject.controller.FormParam;
import io.dinject.controller.Header;
import io.dinject.controller.QueryParam;
import io.dinject.javalin.generator.openapi.MethodDocBuilder;
import io.dinject.javalin.generator.openapi.MethodParamDocBuilder;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class ElementReader {

  private final ProcessingContext ctx;
  private final Element element;
  private final String rawType;
  private final TypeHandler typeHandler;
  private final String varName;
  private final String snakeName;
  private final boolean formMarker;

  private String paramName;
  private ParamType paramType;
  private boolean impliedParamType;
  private String paramDefault;

  ElementReader(Element element, ProcessingContext ctx, ParamType defaultType, boolean formMarker) {
    this(element, element.asType().toString(), ctx, defaultType, formMarker);
  }

  ElementReader(Element element, String rawType, ProcessingContext ctx, ParamType defaultType, boolean formMarker) {
    this.ctx = ctx;
    this.element = element;
    this.rawType = rawType;
    this.typeHandler = TypeMap.get(rawType);
    this.formMarker = formMarker;

    this.varName = element.getSimpleName().toString();
    this.snakeName = Util.snakeCase(varName);
    this.paramName = varName;

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
      this.impliedParamType = true;
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

  public String getVarName() {
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

  /**
   * Build the OpenAPI documentation for this parameter.
   */
  void buildApiDocumentation(MethodDocBuilder methodDoc) {
    if (!isJavalinContext()) {
      new MethodParamDocBuilder(methodDoc, this).build();
    }
  }

  void writeCtxGet(Append writer, PathSegments segments) {

    if (isJavalinContext()) {
      // no conversion for this parameter
      return;
    }

    String shortType = shortType();
    writer.append("      %s %s = ", shortType, varName);
    if (setValue(writer, segments, shortType)) {
      writer.append(";").eol();
    }
  }

  void setValue(Append writer) {
    setValue(writer, PathSegments.EMPTY, shortType());
  }

  private boolean setValue(Append writer, PathSegments segments, String shortType) {

    if (formMarker && impliedParamType && typeHandler == null) {
      // @Form on method and this type is a "bean" so treat is as a form bean
      writeForm(writer, shortType, varName, ParamType.FORMPARAM);
      paramType = ParamType.FORM;
      return false;
    }

    if (ParamType.FORM == paramType) {
      writeForm(writer, shortType, varName, ParamType.FORMPARAM);
      return false;
    }
    if (ParamType.BEANPARAM == paramType) {
      writeForm(writer, shortType, varName, ParamType.QUERYPARAM);
      return false;
    }

    if (impliedParamType) {
      PathSegments.Segment segment = segments.segment(varName);
      if (segment != null) {
        // path or metric parameter
        boolean requiredParam = segment.isRequired(varName);
        String asMethod = (typeHandler == null) ? null : (requiredParam) ? typeHandler.asMethod() : typeHandler.toMethod();

        if (asMethod != null) {
          writer.append(asMethod);
        }
        segment.writeGetVal(writer, varName);
        if (asMethod != null) {
          writer.append(")");
        }
        paramType = ParamType.PATHPARAM;
        return true;
      }
    }

    String asMethod = (typeHandler == null) ? null : typeHandler.toMethod();

    if (asMethod != null) {
      writer.append(asMethod);
    }

    if (typeHandler == null) {
      // assuming this is a body (POST, PATCH)
      writer.append("ctx.bodyAsClass(%s.class)", shortType);
      paramType = ParamType.BODY;

    } else {
      if (hasParamDefault()) {
        writer.append("ctx.%s(\"%s\",\"%s\")", paramType, paramName, paramDefault);
      } else {
        writer.append("ctx.%s(\"%s\")", paramType, paramName);
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

  public ParamType getParamType() {
    return paramType;
  }

  public String getParamName() {
    return paramName;
  }

  public String getRawType() {
    return rawType;
  }

  public Element getElement() {
    return element;
  }
}
