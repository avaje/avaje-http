package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ParamType.RESPONSE_HANDLER;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.avaje.http.generator.core.openapi.MethodDocBuilder;
import io.avaje.http.generator.core.openapi.MethodParamDocBuilder;

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
  private String matrixParamName;
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
    final var elementType = ctx.typeElement(rawType);
    return elementType != null
        && (ValidPrism.getInstanceOn(elementType) != null
            || JavaxValidPrism.getInstanceOn(elementType) != null);
  }

  private void readAnnotations(Element element, ParamType defaultType) {

    notNullKotlin = NotNullPrism.getInstanceOn(element) != null;

    final var defaultVal = DefaultPrism.getInstanceOn(element);
    if (defaultVal != null) {
      this.paramDefault = defaultVal.value();
    }
    final var form = FormPrism.getInstanceOn(element);
    if (form != null) {
      this.paramType = ParamType.FORM;
      return;
    }
    final var beanParam = BeanParamPrism.getInstanceOn(element);
    if (beanParam != null) {
      this.paramType = ParamType.BEANPARAM;
      return;
    }
    final var queryParam = QueryParamPrism.getInstanceOn(element);
    if (queryParam != null) {
      this.paramName = nameFrom(queryParam.value(), varName);
      this.paramType = ParamType.QUERYPARAM;
      return;
    }
    final var formParam = FormParamPrism.getInstanceOn(element);
    if (formParam != null) {
      this.paramName = nameFrom(formParam.value(), varName);
      this.paramType = ParamType.FORMPARAM;
      return;
    }
    final var cookieParam = CookiePrism.getInstanceOn(element);
    if (cookieParam != null) {
      this.paramName = nameFrom(cookieParam.value(), varName);
      this.paramType = ParamType.COOKIE;
      this.paramDefault = null;
      return;
    }
    final var headerParam = HeaderPrism.getInstanceOn(element);
    if (headerParam != null) {
      this.paramName = nameFrom(headerParam.value(), Util.initcapSnake(snakeName));
      this.paramType = ParamType.HEADER;
      this.paramDefault = null;
      return;
    }

    final var matrixParam = MatrixParamPrism.getInstanceOn(element);
    if (matrixParam != null) {
      this.matrixParamName = nameFrom(matrixParam.value(), varName);
      this.paramType = defaultType;
      this.impliedParamType = true;
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

  public String varName() {
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

  private String handlerShortType() {
    if (typeHandler != null) {
      return typeHandler.shortName();
    } else {
      return shortType;
    }
  }

  void addImports(ControllerReader bean) {
    if (typeHandler != null) {
      String importType = typeHandler.importType();
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
    if (isPlatformContext()
        || (paramType == ParamType.BODY && ctx.platform().isBodyMethodParam())) {
      // body passed as method parameter (Helidon)
      return;
    }
    String shortType = handlerShortType();
    writer.append("%s  var %s = ", ctx.platform().indent(), varName);
    if (setValue(writer, segments, shortType)) {
      writer.append(";").eol();
    }
  }

  void setValue(Append writer) {
    setValue(writer, PathSegments.EMPTY, handlerShortType());
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
      var name = matrixParamName != null ? matrixParamName : varName;
      PathSegments.Segment segment = segments.segment(name);
      if (segment != null) {
        // path or matrix parameter
        boolean requiredParam = segment.isRequired(varName);
        String asMethod =
            (typeHandler == null)
                ? null
                : (requiredParam) ? typeHandler.asMethod() : typeHandler.toMethod();
        if (asMethod != null) {
          writer.append(asMethod);
        }
        segment.writeGetVal(writer, name, ctx.platform());
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
      writer.append(ctx.platform().bodyAsClass(type));

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
    TypeElement formBeanType = ctx.typeElement(rawType);
    BeanParamReader form = new BeanParamReader(ctx, formBeanType, varName, shortType, defaultParamType);
    form.write(writer);
  }

  public ParamType paramType() {
    return paramType;
  }

  public String paramName() {
    return paramName;
  }

  public String shortType() {
    return shortType;
  }

  public String rawType() {
    return rawType;
  }

  public UType type() {
    return type;
  }

  public Element element() {
    return element;
  }

  public void setResponseHandler() {
    paramType = RESPONSE_HANDLER;
  }
}
