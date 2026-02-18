package io.avaje.http.generator.core.openapi;

import static io.avaje.http.generator.core.Util.typeDef;

import io.avaje.http.generator.core.javadoc.Javadoc;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.avaje.http.generator.core.APContext;
import io.avaje.http.generator.core.HiddenPrism;
import io.avaje.http.generator.core.Util;
import io.avaje.prism.GeneratePrism;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

/** Help build OpenAPI Schema objects. */
@GeneratePrism(jakarta.validation.constraints.Size.class)
@GeneratePrism(jakarta.validation.constraints.Email.class)
@GeneratePrism(value = javax.validation.constraints.Size.class, name = "JavaxSizePrism")
@GeneratePrism(value = javax.validation.constraints.Email.class, name = "JavaxEmailPrism")
class SchemaDocBuilder {

  private static final String APP_FORM = "application/x-www-form-urlencoded";
  private static final String APP_JSON = "application/json";

  private final Elements elements;
  private final Types types;
  private final KnownTypes knownTypes;
  private final TypeMirror iterableType;
  private final TypeMirror mapType;
  private final TypeMirror completableFutureType;

  private final Map<String, Schema> schemas = new TreeMap<>();

  SchemaDocBuilder(Types types, Elements elements) {
    this.types = types;
    this.elements = elements;
    this.knownTypes = new KnownTypes();
    this.iterableType = types.erasure(elements.getTypeElement("java.lang.Iterable").asType());
    this.mapType = types.erasure(elements.getTypeElement("java.util.Map").asType());
    this.completableFutureType = types.erasure(elements.getTypeElement("java.util.concurrent.CompletableFuture").asType());
  }

  Map<String, Schema> getSchemas() {
    return schemas;
  }

  Content createContent(TypeMirror returnType, String mediaType) {
    MediaType mt = new MediaType();
    mt.setSchema(toSchema(returnType));
    Content content = new Content();
    content.addMediaType(mediaType, mt);
    return content;
  }

  /**
   * Add parameter as a form parameter.
   */
  void addFormParam(Operation operation, String varName, Schema schema) {
    RequestBody body = requestBody(operation);
    Schema formSchema = requestFormParamSchema(body);
    formSchema.addProperties(varName, schema);
  }

  private Schema requestFormParamSchema(RequestBody body) {
    final Content content = body.getContent();
    MediaType mediaType = content.get(APP_FORM);

    Schema schema;
    if (mediaType != null) {
      schema = mediaType.getSchema();
    } else {
      schema = new Schema();
      schema.setType("object");
      mediaType = new MediaType();
      mediaType.schema(schema);
      content.addMediaType(APP_FORM, mediaType);
    }
    return schema;
  }

  /** Add as request body. */
  void addRequestBody(Operation operation, Schema schema, String mediaType, String description) {
    RequestBody body = requestBody(operation);
    body.setDescription(description);

    MediaType mt = new MediaType();
    mt.schema(schema);

    body.getContent().addMediaType(mediaType, mt);
  }

  private RequestBody requestBody(Operation operation) {
    RequestBody body = operation.getRequestBody();
    if (body == null) {
      body = new RequestBody();
      body.setRequired(true);
      Content content = new Content();
      body.setContent(content);
      operation.setRequestBody(body);
    }
    return body;
  }

  private static TypeMirror typeArgument(TypeMirror type) {
    List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
    return typeArguments.get(0);
  }

  Schema<?> toSchema(Element element) {
    final var schema = toSchema(element.asType());
    setLengthMinMax(element, schema);
    setFormatFromValidation(element, schema);
    if (isNotNullable(element)) {
      schema.setNullable(Boolean.FALSE);
    }
    return schema;
  }

  Schema<?> toSchema(TypeMirror type) {
    if (types.isAssignable(type, completableFutureType)) {
      type = typeArgument(type);
    }
    Schema<?> schema = knownTypes.createSchema(typeDef(type));
    if (schema != null) {
      return schema;
    }
    if (types.isAssignable(type, mapType)) {
      return buildMapSchema(type);
    }
    if (type.getKind() == TypeKind.ARRAY) {
      return buildArraySchema(type);
    }
    if (types.isAssignable(type, iterableType)) {
      return buildIterableSchema(type);
    }
    Element e = types.asElement(type);
    if (e != null && e.getKind() == ElementKind.ENUM) {
      return buildEnumSchema(e);
    }
    return buildObjectSchema(type);
  }

  private Schema<?> buildEnumSchema(Element e) {
    var schema = new StringSchema();
    e.getEnclosedElements().stream()
      .filter(ec -> ElementKind.ENUM_CONSTANT.equals(ec.getKind()))
      .forEach(ec -> schema.addEnumItem(ec.getSimpleName().toString()));

    var doc = Javadoc.parse(elements.getDocComment(e));
    var desc = doc.getDescription();
    if (desc != null && !desc.isEmpty()) {
      schema.setDescription(desc);
    }
    return schema;
  }

  private Schema<?> buildObjectSchema(TypeMirror type) {
    String objectSchemaKey = getObjectSchemaName(type);

    Schema objectSchema = schemas.get(objectSchemaKey);
    if (objectSchema == null) {
      // Put first to resolve recursive stack overflow
      objectSchema = new ObjectSchema();
      schemas.put(objectSchemaKey, objectSchema);
      populateObjectSchema(type, objectSchema);
    }

    Schema ref = new Schema();
    ref.$ref("#/components/schemas/" + objectSchemaKey);
    return ref;
  }

  private Schema<?> buildIterableSchema(TypeMirror type) {
    Schema<?> itemSchema = new ObjectSchema().format("unknownIterableType");
    if (type.getKind() == TypeKind.DECLARED) {
      List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
      if (typeArguments.size() == 1) {
        TypeMirror typeMirror = typeArguments.get(0);
        itemSchema = toSchema(typeMirror);
        if (isNotNullable(typeMirror)) {
          itemSchema.setNullable(Boolean.FALSE);
        }
      }
    }

    ArraySchema arraySchema = new ArraySchema();
    arraySchema.setItems(itemSchema);
    return arraySchema;
  }

  private Schema<?> buildArraySchema(TypeMirror type) {
    ArrayType arrayType = (ArrayType) type;
    Schema<?> itemSchema = toSchema(arrayType.getComponentType());

    ArraySchema arraySchema = new ArraySchema();
    arraySchema.setItems(itemSchema);
    return arraySchema;
  }

  private Schema<?> buildMapSchema(TypeMirror type) {
    Schema<?> valueSchema = new ObjectSchema().format("unknownMapValueType");

    if (type.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) type;
      List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
      if (typeArguments.size() == 2) {
        TypeMirror valueType = typeArguments.get(1);
        valueSchema = toSchema(valueType);
        if (isNotNullable(valueType)) {
          valueSchema.setNullable(Boolean.FALSE);
        }
      }
    }

    MapSchema mapSchema = new MapSchema();
    mapSchema.setAdditionalProperties(valueSchema);
    return mapSchema;
  }

  private String getObjectSchemaName(TypeMirror type) {
    var canonicalName = Util.trimAnnotations(type.toString());
    final var pos = canonicalName.lastIndexOf('.');
    if (pos > -1) {
      canonicalName = canonicalName.substring(pos + 1);
    }
    return canonicalName;
  }

  private <T> void populateObjectSchema(TypeMirror objectType, Schema<T> objectSchema) {
    Element element = types.asElement(objectType);
    for (VariableElement field : allFields(element)) {
      Schema<?> propSchema = toSchema(field.asType());
      if (isNotNullable(field)) {
        propSchema.setNullable(Boolean.FALSE);
        objectSchema.addRequiredItem(field.getSimpleName().toString());
      }
      setDescription(field, propSchema);
      setLengthMinMax(field, propSchema);
      setFormatFromValidation(field, propSchema);
      objectSchema.addProperties(field.getSimpleName().toString(), propSchema);
    }
  }

  private void setFormatFromValidation(Element element, Schema<?> propSchema) {
    if (EmailPrism.isPresent(element) || JavaxEmailPrism.isPresent(element)) {
      propSchema.setFormat("email");
    }
  }

  private void setDescription(Element element, Schema<?> propSchema) {
    var doc = Javadoc.parse(elements.getDocComment(element));

    if (!doc.getSummary().isEmpty()) {
      propSchema.setDescription(doc.getSummary());
      return;
    }
    try {

      final var enclosingElement = element.getEnclosingElement();
      if (enclosingElement.getKind() == ElementKind.valueOf("RECORD")) {
        Optional.of(Javadoc.parse(elements.getDocComment(enclosingElement)))
            .map(d -> d.getParams().get(element.getSimpleName().toString()))
            .ifPresent(propSchema::setDescription);
      }
    } catch (IllegalArgumentException e) {
      // not on jdk 16+
    }
  }

  private void setLengthMinMax(Element element, Schema<?> propSchema) {
    SizePrism.getOptionalOn(element)
        .ifPresent(
            size -> {
              if (size.min() > 0) {
                propSchema.setMinLength(size.min());
              }
              if (size.max() > 0) {
                propSchema.setMaxLength(size.max());
              }
            });

    JavaxSizePrism.getOptionalOn(element)
        .ifPresent(
            size -> {
              if (size.min() > 0) {
                propSchema.setMinLength(size.min());
              }
              if (size.max() > 0) {
                propSchema.setMaxLength(size.max());
              }
            });
  }

  private boolean isNotNullable(Element element) {
    List<AnnotationMirror> annotationMirrors = new ArrayList<>();
    if (element instanceof VariableElement) {
      annotationMirrors.addAll(element.asType().getAnnotationMirrors());
    } else {
      annotationMirrors.addAll(element.getAnnotationMirrors());
    }

    if (Util.nullMarked(element)) {
      for (var mirror : annotationMirrors) {
        if ("Nullable"
            .equals(APContext.asTypeElement(mirror.getAnnotationType()).getSimpleName().toString())) {
          return false;
        }
      }
      return true;
    }

    return annotationMirrors.stream()
      .anyMatch(m -> m.toString().contains("@") &&
        Stream.of("NotNull", "NotEmpty", "NotBlank")
          .anyMatch(annotation -> m.toString().contains(annotation))
      );
  }

  private boolean isNotNullable(TypeMirror type) {
    List<? extends AnnotationMirror> annotationMirrors = type.getAnnotationMirrors();

    for (AnnotationMirror annotationMirror : annotationMirrors) {
      if ("org.jspecify.annotations.Nullable".equals(annotationMirror.getAnnotationType().asElement().toString())) {
      return false;
      }
    }
    return true;
  }

  /**
   * Gather all the fields (properties) for the given bean element.
   */
  private List<VariableElement> allFields(Element element) {
    List<VariableElement> list = new ArrayList<>();
    gatherProperties(list, element);
    return list;
  }

  /**
   * Recursively gather all the fields (properties) for the given bean element.
   */
  private void gatherProperties(List<VariableElement> fields, Element element) {
    if (element == null) {
      return;
    }
    if (element instanceof TypeElement) {
      Element mappedSuper = types.asElement(((TypeElement) element).getSuperclass());
      if (mappedSuper != null
              && !"java.lang.Object".equals(mappedSuper.toString())
              && !"java.lang.Record".equals(mappedSuper.toString())) {
        gatherProperties(fields, mappedSuper);
      }
      for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
        if (!ignoreField(field)) {
          fields.add(field);
        }
      }
    }
  }

  /**
   * Ignore static or transient fields.
   */
  private boolean ignoreField(VariableElement field) {
    return isStaticOrTransient(field) || isHiddenField(field);
  }

  private boolean isHiddenField(VariableElement field) {
    if (HiddenPrism.isPresent(field)) {
      return true;
    }
    for (AnnotationMirror annotationMirror : field.getAnnotationMirrors()) {
      String simpleName = annotationMirror.getAnnotationType().asElement().getSimpleName().toString();
      if ("JsonIgnore".equals(simpleName)) {
        return true;
      }
    }
    return false;
  }

  private boolean isStaticOrTransient(VariableElement field) {
    Set<Modifier> modifiers = field.getModifiers();
    return (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT));
  }

  private boolean isNullMarked(Element element) {
    Element classElement;
    // Because this is build with Java 11 records are not supported yet.
    switch(element.getKind().toString()) {
      case "CLASS":
      case "RECORD":
        classElement = element;
      break;
      case "FIELD":
      case "ENUM":
        classElement = element.getEnclosingElement();
      break;
      case "PARAMETER":
        classElement = element.getEnclosingElement().getEnclosingElement();
      break;
      default:
        classElement = null;
    }

    if (classElement == null) {
      // "No class found for on top of element Type"
      return false;
    }

    for (AnnotationMirror annotationMirror : classElement.getAnnotationMirrors()) {
      if (annotationMirror.toString().contains("@org.jspecify.annotations.NullMarked")) {
        return true;
      }
    }
    return false;
  }
}
