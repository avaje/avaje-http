package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ParamType.RESPONSE_HANDLER;
import static io.avaje.http.generator.core.ProcessingContext.platform;
import static io.avaje.http.generator.core.ProcessingContext.typeElement;
import static io.avaje.http.generator.core.ProcessingContext.logError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;

import io.avaje.http.generator.core.openapi.MethodDocBuilder;
import io.avaje.http.generator.core.openapi.MethodParamDocBuilder;

public class ElementReader {

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
  private final boolean specialParam;

  private String paramName;
  private ParamType paramType;
  private String matrixParamName;
  private boolean impliedParamType;
  private List<String> paramDefault;

  private boolean notNullKotlin;
  private boolean isParamCollection;
  private boolean isParamMap;
  private final Set<String> imports = new HashSet<>();

  private final List<String> validationGroups = new ArrayList<>();

  ElementReader(Element element, ParamType defaultType, boolean formMarker) {
    this(element, null, Util.typeDef(element.asType()), defaultType, formMarker);
  }

  ElementReader(Element element, UType type, String rawType, ParamType defaultType, boolean formMarker) {
    this.element = element;
    this.type = type;
    this.rawType = rawType;
    this.shortType = Util.shortName(rawType);
    this.contextType = platform().isContextType(rawType);

    this.specialParam =
        defaultType == ParamType.FORMPARAM
            || defaultType == ParamType.QUERYPARAM
            || defaultType == ParamType.HEADER
            || defaultType == ParamType.COOKIE;

    typeHandler = initTypeHandler();

    this.imports.add(rawType);
    if (typeHandler != null) {
      this.imports.addAll(typeHandler.importTypes());
    }

    this.formMarker = formMarker;
    this.varName = element.getSimpleName().toString();
    this.snakeName = Util.snakeCase(varName);
    this.paramName = varName;
    if (!contextType) {
      readAnnotations(element, defaultType);
      useValidation = useValidation();
      HttpValidPrism.getOptionalOn(element).map(HttpValidPrism::groups).stream()
          .flatMap(List::stream)
          .map(TypeMirror::toString)
          .forEach(validationGroups::add);
      this.imports.addAll(validationGroups);
    } else {
      paramType = ParamType.CONTEXT;
      useValidation = false;
    }
    if (ParamType.FORM == paramType || ParamType.BEANPARAM == paramType) {
      beanParamImports(rawType);
    }
  }

  private void beanParamImports(String rawType) {
    typeElement(rawType).getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.FIELD)
        .filter(f -> !IgnorePrism.isPresent(f))
        .map(Element::asType)
        .map(UType::parse)
        .flatMap(u -> u.importTypes().stream())
        .forEach(imports::add);
  }

  TypeHandler initTypeHandler() {

    if (specialParam) {

      final var typeOp =
          Optional.ofNullable(type).or(() -> Optional.of(UType.parse(element.asType())));

      final var mainTypeEnum =
          typeOp
              .flatMap(t -> Optional.ofNullable(typeElement(t.mainType())))
              .map(TypeElement::getKind)
              .filter(ElementKind.ENUM::equals)
              .isPresent();

      final var isCollection =
          typeOp
              .filter(t -> t.isGeneric() && !t.mainType().startsWith("java.util.Map"))
              .isPresent();

      final var isMap =
          !isCollection && typeOp.filter(t -> t.mainType().startsWith("java.util.Map")).isPresent();

      if (mainTypeEnum) {
        return TypeMap.enumParamHandler(typeOp.orElseThrow());
      } else if (isCollection) {
        this.isParamCollection = true;
        final var isEnumCollection =
            typeOp
                .flatMap(t -> Optional.ofNullable(typeElement(t.param0())))
                .map(TypeElement::getKind)
                .filter(ElementKind.ENUM::equals)
                .isPresent();

        return TypeMap.collectionHandler(typeOp.orElseThrow(), isEnumCollection);
      } else if (isMap) {
        this.isParamMap = true;

        return new TypeMap.StringHandler();
      }
    }

    return TypeMap.get(rawType);
  }

  private boolean useValidation() {
    if (typeHandler != null) {
      return false;
    }
    final var elementType = typeElement(rawType);
    return elementType != null && ValidPrism.isPresent(elementType);
  }

  private void readAnnotations(Element element, ParamType defaultType) {

    notNullKotlin = NotNullPrism.getInstanceOn(element) != null;

    final var defaultVal = DefaultPrism.getInstanceOn(element);
    if (defaultVal != null) {
      this.paramDefault = defaultVal.value();
    }
    if (FormPrism.isPresent(element)) {
      this.paramType = ParamType.FORM;
      return;
    }
    if (BeanParamPrism.isPresent(element)) {
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

    if ("java.lang.String".equals(element.asType().toString())
        && BodyStringPrism.isPresent(element)) {
      this.paramType = ParamType.BODY;
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
    return platform().platformVariable(rawType);
  }

  private String handlerShortType() {
    if (typeHandler != null) {
      return typeHandler.shortName();
    } else {
      return shortType;
    }
  }

  void addImports(ControllerReader bean) {

    bean.addImportTypes(imports);
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
    if (!isPlatformContext()
        && !isParamMap
        && paramType != ParamType.BEANPARAM
        && !IgnorePrism.isPresent(element)) {
      new MethodParamDocBuilder(methodDoc, this).build();
    }
  }

  void writeValidate(Append writer) {
    if (!contextType && typeHandler == null) {
      final var indent = platform().indent();
      if (useValidation) {
        writer.append("%s  validator.validate(%s, ", indent, varName);
        platform().writeAcceptLanguage(writer);
        validationGroups.forEach(g -> writer.append(", %s", Util.shortName(g)));
        writer.append(");").eol();
      } else {
        writer.append("%s  // no validation required on %s", indent, varName).eol();
      }
    }
  }

  void writeCtxGet(Append writer, PathSegments segments) {
    if (isPlatformContext()
        || (paramType == ParamType.BODY && platform().isBodyMethodParam())) {
      // body passed as method parameter (Helidon)
      return;
    }
    final String shortType = handlerShortType();
    writer.append("%s  var %s = ", platform().indent(), varName);
    if (setValue(writer, segments, shortType)) {
      writer.append(";").eol();
    }
  }

  void setValue(Append writer) {
    try {
      setValue(writer, PathSegments.EMPTY, handlerShortType());
    } catch (final UnsupportedOperationException e) {
      logError(element, e.getMessage());
    }
  }

  private boolean setValue(Append writer, PathSegments segments, String shortType) {

    if (ParamType.FORM == paramType) {
      writeForm(writer, shortType, varName, ParamType.FORMPARAM);
      return false;
    }
    if (ParamType.BEANPARAM == paramType) {
      writeForm(writer, shortType, varName, ParamType.QUERYPARAM);
      return false;
    }
    if (impliedParamType) {
      final var name = matrixParamName != null ? matrixParamName : varName;
      final PathSegments.Segment segment = segments.segment(name);
      if (segment != null) {
        // path or matrix parameter
        final boolean requiredParam = segment.isRequired(varName);
        final String asMethod =
            (typeHandler == null)
                ? null
                : (requiredParam) ? typeHandler.asMethod() : typeHandler.toMethod();
        if (asMethod != null) {
          writer.append(asMethod);
        }
        segment.writeGetVal(writer, name, platform());
        if (asMethod != null) {
          writer.append(")");
        }
        paramType = ParamType.PATHPARAM;
        return true;
      }
    }

    final String asMethod = (typeHandler == null) ? null : typeHandler.toMethod();
    if (asMethod != null) {
      writer.append(asMethod);
    }

    if (typeHandler == null || paramType == ParamType.BODY) {
      // this is a body (POST, PATCH)
      writer.append(platform().bodyAsClass(type));

    } else if (isParamCollection && specialParam) {
      if (hasParamDefault()) {
        platform().writeReadCollectionParameter(writer, paramType, paramName, paramDefault);
      } else {
        platform().writeReadCollectionParameter(writer, paramType, paramName);
      }
    } else if (isParamMap) {
      platform().writeReadMapParameter(writer, paramType);
    } else if (hasParamDefault()) {
      platform().writeReadParameter(writer, paramType, paramName, paramDefault.get(0));
    } else {
      final var checkNull =
          notNullKotlin || (paramType == ParamType.FORMPARAM && typeHandler.isPrimitive());
      if (checkNull) {
        writer.append("checkNull(");
      }
      platform().writeReadParameter(writer, paramType, paramName);
      // writer.append("%s(\"%s\")", paramType, paramName);
      if (checkNull) {
        writer.append(", \"%s\")", paramName);
      }
    }

    if (asMethod != null) {
      writer.append(")");
    }
    return true;
  }

  private void writeForm(Append writer, String shortType, String varName, ParamType defaultParamType) {
    final TypeElement formBeanType = typeElement(rawType);
    final BeanParamReader form = new BeanParamReader(formBeanType, varName, shortType, defaultParamType);
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
