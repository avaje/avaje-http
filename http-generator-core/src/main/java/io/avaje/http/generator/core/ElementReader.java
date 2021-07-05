package io.avaje.http.generator.core;

import io.avaje.http.api.*;
import io.avaje.http.generator.core.openapi.MethodDocBuilder;
import io.avaje.http.generator.core.openapi.MethodParamDocBuilder;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.validation.Valid;

import static io.avaje.http.generator.core.ParamType.RESPONSE_HANDLER;

public class ElementReader {

  private final ProcessingContext ctx;
  private final Element element;
  private final UType type;
  private final String rawType;
  private final String shortType;
  private final TypeHandler typeHandler;
  private final String varName;
  private final String snakeName;
  private final boolean formMarker;
  private final boolean contextType;
  private final boolean useValidation;

  private String paramName;
  private ParamType paramType;
  private boolean impliedParamType;
  private String paramDefault;

  private boolean notNullKotlin;
  //private boolean notNullJavax;

  ElementReader(Element element, ProcessingContext ctx, ParamType defaultType, boolean formMarker) {
    this(element, null, Util.typeDef(element.asType()), ctx, defaultType, formMarker);
  }

  ElementReader(Element element, UType type, String rawType, ProcessingContext ctx, ParamType defaultType, boolean formMarker) {
    this.ctx = ctx;
    this.element = element;
    this.type = type;
    this.rawType = rawType;
    this.shortType = Util.shortName(rawType);
    this.contextType = ctx.platform().isContextType(rawType);
    this.typeHandler = TypeMap.get(rawType);
    this.formMarker = formMarker;
    this.varName = element.getSimpleName().toString();
    this.snakeName = Util.snakeCase(varName);
    this.paramName = varName;
    if (!contextType) {
      readAnnotations(element, defaultType);
      useValidation = useValidation();
    } else {
      paramType = ParamType.CONTEXT;
      useValidation = false;
    }
  }

  private boolean useValidation() {
    if (typeHandler != null) {
      return false;
    }
    TypeElement elementType = ctx.getTypeElement(rawType);
    return elementType != null && elementType.getAnnotation(Valid.class) != null;
  }

  private void readAnnotations(Element element, ParamType defaultType) {

    notNullKotlin = (element.getAnnotation(org.jetbrains.annotations.NotNull.class) != null);
    //notNullJavax = (element.getAnnotation(javax.validation.constraints.NotNull.class) != null);

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
      this.impliedParamType = true;
      if (typeHandler != null) {
        // a scalar type that we know how to convert
        this.paramType = defaultType;
      } else {
        this.paramType = formMarker ? ParamType.FORM : ParamType.BODY;
      }
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

  private boolean isPlatformContext() {
    return contextType;
  }

  private String platformVariable() {
    return ctx.platform().platformVariable(rawType);
  }

  private String shortType() {
    if (typeHandler != null) {
      return typeHandler.shortName();
    } else {
      return shortType;
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
    if (isPlatformContext()) {
      writer.append(platformVariable());
    } else {
      writer.append(varName);
    }
  }

  /**
   * Build the OpenAPI documentation for this parameter.
   */
  void buildApiDocumentation(MethodDocBuilder methodDoc) {
    if (!isPlatformContext()) {
      new MethodParamDocBuilder(methodDoc, this).build();
    }
  }

  void writeValidate(Append writer) {
    if (!contextType && typeHandler == null) {
      if (useValidation) {
        writer.append("validator.validate(%s);", varName).eol();
      } else {
        writer.append("// no validation required on %s", varName).eol();
      }
      writer.append("      ");
    }
  }

  void writeCtxGet(Append writer, PathSegments segments) {
    if (isPlatformContext()) {
      // no conversion for this parameter
      return;
    }
    if (paramType == ParamType.BODY && ctx.platform().isBodyMethodParam()) {
      // body passed as method parameter (Helidon)
      return;
    }
    String shortType = shortType();
    writer.append("%s  %s %s = ", ctx.platform().indent(), shortType, varName);
    if (setValue(writer, segments, shortType)) {
      writer.append(";").eol();
    }
  }

  void setValue(Append writer) {
    setValue(writer, PathSegments.EMPTY, shortType());
  }

  private boolean setValue(Append writer, PathSegments segments, String shortType) {
//    if (formMarker && impliedParamType && typeHandler == null) {
//      if (ParamType.FORM != paramType) {
//        throw new IllegalStateException("Don't get here?");
//      }
////      // @Form on method and this type is a "bean" so treat is as a form bean
////      writeForm(writer, shortType, varName, ParamType.FORMPARAM);
////      paramType = ParamType.FORM;
////      return false;
//    }
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
        // path or matrix parameter
        boolean requiredParam = segment.isRequired(varName);
        String asMethod = (typeHandler == null) ? null : (requiredParam) ? typeHandler.asMethod() : typeHandler.toMethod();
        if (asMethod != null) {
          writer.append(asMethod);
        }
        segment.writeGetVal(writer, varName, ctx.platform());
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
      // this is a body (POST, PATCH)
      writer.append(ctx.platform().bodyAsClass(shortType));

    } else {
      if (hasParamDefault()) {
        ctx.platform().writeReadParameter(writer, paramType, paramName, paramDefault);
      } else {
        boolean checkNull = notNullKotlin || (paramType == ParamType.FORMPARAM && typeHandler.isPrimitive());
        if (checkNull) {
          writer.append("checkNull(");
        }
        ctx.platform().writeReadParameter(writer, paramType, paramName);
        //writer.append("ctx.%s(\"%s\")", paramType, paramName);
        if (checkNull) {
          writer.append(", \"%s\")", paramName);
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

  public ParamType getParamType() {
    return paramType;
  }

  public String getParamName() {
    return paramName;
  }

  public String getShortType() {
    return shortType;
  }

  public String getRawType() {
    return rawType;
  }

  public UType getType() {
    return type;
  }

  public Element getElement() {
    return element;
  }

  public void setResponseHandler() {
    paramType = RESPONSE_HANDLER;
  }
}
