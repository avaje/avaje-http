package io.avaje.http.maven.openapi.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.media.ArbitrarySchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.EmailSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.PasswordSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;
import io.swagger.v3.oas.models.media.XML;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * Unfortunately, cannot be automatically generated because of the generics
 *   applied to fields that are private (mixins cannot override those to be ignored)
 */
@CustomAdapter
public final class SchemaCustomAdaptor implements JsonAdapter<Schema> {
  private final PropertyNames names;
  private final JsonAdapter<String> stringJsonAdapter;
  private final JsonAdapter<BigDecimal> bigDecimalJsonAdapter;
  private final JsonAdapter<Boolean> booleanJsonAdapter;
  private final JsonAdapter<Integer> integerJsonAdapter;
  private final JsonAdapter<List<String>> listStringJsonAdaptor;
  private final JsonAdapter<ExternalDocumentation> externalDocumentationJsonAdapter;
  private final JsonAdapter<XML> xmlJsonAdapter;
  private final JsonAdapter<Map<String, Object>> mapStringObjectJsonAdaptor;
  private final JsonAdapter<Discriminator> discriminatorJsonAdapter;
  private final JsonAdapter<Set<String>> setStringJsonAdaptor;
  private final JsonAdapter<Map<String, List<String>>> mapStringListStringJsonAdaptor;
  private final JsonAdapter<byte[]> byteArrayJsonAdaptor;
  private final JsonAdapter<List<byte[]>> listByteArrayJsonAdaptor;
  private final JsonAdapter<List<Boolean>> listBooleanAdaptor;
  private final JsonAdapter<Date> dateJsonAdaptor;
  private final JsonAdapter<List<Date>> listDateJsonAdaptor;
  private final JsonAdapter<OffsetDateTime> odtJsonAdaptor;
  private final JsonAdapter<List<OffsetDateTime>> listOdtJsonAdaptor;
  private final JsonAdapter<BigDecimal> numberJsonAdapter;
  private final JsonAdapter<List<BigDecimal>> listNumberJsonAdaptor;
  private final JsonAdapter<List<BigDecimal>> listBigDecimalJsonAdaptor;
  private final JsonAdapter<UUID> uuidJsonAdapter;
  private final JsonAdapter<List<UUID>> listUuidJsonAdaptor;
  private final JsonAdapter<Object> objectJsonAdapter;
  private final JsonAdapter<List<Object>> objectListJsonAdapter;

  public SchemaCustomAdaptor(final Jsonb jsonb) {
    names = jsonb.properties(
      "type", "format", "name", "title", "multipleOf", "maximum",
      "exclusiveMaximum",
      "minimum", "exclusiveMinimum", "maxLength", "minLength",
      "pattern", "maxItems", "minItems", "uniqueItems", "maxProperties", "minProperties",
      "required", "not", "properties", "additionalProperties", "description",
      "$ref", "nullable", "readOnly", "writeOnly",
      "externalDocs", "deprecated", "xml", "extensions",
      "discriminator", "exampleSetFlag", "prefixItems", "allOf",
      "anyOf", "oneOf", "items", "specVersion", "types", "patternProperties",
      "exclusiveMaximumValue", "exclusiveMinimumValue", "contains",
      "$id", "$schema", "$anchor", "$vocabulary", "$dynamicAnchor",
      "$dynamicRef", "contentEncoding", "contentMediaType", "contentSchema",
      "propertyNames", "unevaluatedProperties", "maxContains",
      "minContains", "additionalItems", "unevaluatedItems", "if", "else", "then",
      "dependentSchemas",
      "dependentRequired", "$comment", "booleanSchemaValue",
      // These are typed
      "examples", "example", "enum", "const"
    );
    stringJsonAdapter = jsonb.adapter(String.class);
    bigDecimalJsonAdapter = jsonb.adapter(BigDecimal.class);
    booleanJsonAdapter = jsonb.adapter(Boolean.class);
    integerJsonAdapter = jsonb.adapter(Integer.class);
    listStringJsonAdaptor = jsonb.adapter(Types.listOf(String.class));
    externalDocumentationJsonAdapter = jsonb.adapter(ExternalDocumentation.class);
    xmlJsonAdapter = jsonb.adapter(XML.class);
    mapStringObjectJsonAdaptor = jsonb.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
    discriminatorJsonAdapter = jsonb.adapter(Discriminator.class);
    setStringJsonAdaptor = jsonb.adapter(Types.setOf(String.class));
    mapStringListStringJsonAdaptor = jsonb.adapter(Types.newParameterizedType(Map.class, String.class, Types.listOf(String.class)));
    byteArrayJsonAdaptor = jsonb.adapter(Types.arrayOf(byte.class));
    listByteArrayJsonAdaptor = jsonb.adapter(Types.listOf(Types.arrayOf(byte.class)));
    listBooleanAdaptor = jsonb.adapter(Types.listOf(Boolean.class));
    dateJsonAdaptor = jsonb.adapter(Date.class);
    listDateJsonAdaptor = jsonb.adapter(Types.listOf(Date.class));
    odtJsonAdaptor = jsonb.adapter(OffsetDateTime.class);
    listOdtJsonAdaptor = jsonb.adapter(Types.listOf(OffsetDateTime.class));
    numberJsonAdapter = jsonb.adapter(BigDecimal.class);
    listNumberJsonAdaptor = jsonb.adapter(Types.listOf(BigDecimal.class));
    listBigDecimalJsonAdaptor = jsonb.adapter(Types.listOf(BigDecimal.class));
    uuidJsonAdapter = jsonb.adapter(UUID.class);
    listUuidJsonAdaptor = jsonb.adapter(Types.listOf(UUID.class));
    objectJsonAdapter = jsonb.adapter(Object.class);
    objectListJsonAdapter = jsonb.adapter(Types.listOf(Object.class));
  }

  @Override
  public void toJson(JsonWriter writer, Schema value) {
    writer.beginObject(names);
    toJsonImpl(writer, value);
    writer.endObject();
  }

  @Override
  public Schema fromJson(JsonReader reader) {
    String name = null;
    String title = null;
    BigDecimal multipleOf = null;
    BigDecimal maximum = null;
    Boolean exclusiveMaximum = null;
    BigDecimal minimum = null;
    Boolean exclusiveMinimum = null;
    Integer maxLength = null;
    Integer minLength = null;
    String pattern = null;
    Integer maxItems = null;
    Integer minItems = null;
    Boolean uniqueItems = null;
    Integer maxProperties = null;
    Integer minProperties = null;
    List<String> required = null;
    String type = null;
    Schema not = null;
    Map<String, Schema> properties = null;
    Object additionalProperties = null;
    String description = null;
    String format = null;
    String $ref = null;
    Boolean nullable = null;
    Boolean readOnly = null;
    Boolean writeOnly = null;
    ExternalDocumentation externalDocs = null;
    Boolean deprecated = null;
    XML xml = null;
    java.util.Map<String, Object> extensions = null;
    Discriminator discriminator = null;
    List<Schema> prefixItems = null;
    List<Schema> allOf = null;
    List<Schema> anyOf = null;
    List<Schema> oneOf = null;
    Schema<?> items = null;
    Set<String> types = null;
    Map<String, Schema> patternProperties = null;
    BigDecimal exclusiveMaximumValue = null;
    BigDecimal exclusiveMinimumValue = null;
    Schema contains = null;
    String $id = null;
    String $schema = null;
    String $anchor = null;
    String $vocabulary = null;
    String $dynamicAnchor = null;
    String $dynamicRef = null;
    String contentEncoding = null;
    String contentMediaType = null;
    Schema contentSchema = null;
    Schema propertyNames = null;
    Schema unevaluatedProperties = null;
    Integer maxContains = null;
    Integer minContains = null;
    Schema additionalItems = null;
    Schema unevaluatedItems = null;
    Schema _if = null;
    Schema _else = null;
    Schema then = null;
    Map<String, Schema> dependentSchemas = null;
    Map<String, List<String>> dependentRequired = null;
    String $comment = null;
    Boolean booleanSchemaValue = null;
    List<Object> examples = null;
    Object example = null;
    List<Object> _enum = null;
    Object _const = null;
    reader.beginObject(names);
    while(reader.hasNextField()) {
      final String fieldName = reader.nextField();
      switch (fieldName) {
        case "type":
          switch (reader.currentToken()) {
            case STRING:
              type = stringJsonAdapter.fromJson(reader);
              break;
            case BEGIN_ARRAY:
              types = setStringJsonAdaptor.fromJson(reader);
              break;
            default:
              reader.skipValue();
          }
          break;
        case "format":
          format = stringJsonAdapter.fromJson(reader);
          break;
        case "name":
          name = stringJsonAdapter.fromJson(reader);
          break;
        case "title":
          title = stringJsonAdapter.fromJson(reader);
          break;
        case "multipleOf":
          multipleOf = bigDecimalJsonAdapter.fromJson(reader);
          break;
        case "maximum":
          maximum = bigDecimalJsonAdapter.fromJson(reader);
          break;
        case "exclusiveMaximum":
          exclusiveMaximum = booleanJsonAdapter.fromJson(reader);
          break;
        case "minimum":
          minimum = bigDecimalJsonAdapter.fromJson(reader);
          break;
        case "exclusiveMinimum":
          exclusiveMinimum = booleanJsonAdapter.fromJson(reader);
          break;
        case "maxLength":
          maxLength = integerJsonAdapter.fromJson(reader);
          break;
        case "minLength":
          minLength = integerJsonAdapter.fromJson(reader);
          break;
        case "pattern":
          pattern = stringJsonAdapter.fromJson(reader);
          break;
        case "maxItems":
          maxItems = integerJsonAdapter.fromJson(reader);
          break;
        case "minItems":
          minItems = integerJsonAdapter.fromJson(reader);
          break;
        case "uniqueItems":
          uniqueItems = booleanJsonAdapter.fromJson(reader);
          break;
        case "maxProperties":
          maxProperties = integerJsonAdapter.fromJson(reader);
          break;
        case "minProperties":
          minProperties = integerJsonAdapter.fromJson(reader);
          break;
        case "required":
          required = listStringJsonAdaptor.fromJson(reader);
          break;
        case "not":
          not = readSelf(reader);
          break;
        case "properties":
          properties = readMapSelf(reader);
          break;
        case "additionalProperties":
          switch (reader.currentToken()) {
            case BOOLEAN:
              additionalProperties = booleanJsonAdapter.fromJson(reader);
              break;
            case BEGIN_OBJECT:
              additionalProperties = fromJson(reader);
              break;
            default:
              reader.skipValue();
          }
          break;
        case "description":
          description = stringJsonAdapter.fromJson(reader);
          break;
        case "$ref":
          $ref = stringJsonAdapter.fromJson(reader);
          break;
        case "nullable":
          nullable = booleanJsonAdapter.fromJson(reader);
          break;
        case "readOnly":
          readOnly = booleanJsonAdapter.fromJson(reader);
          break;
        case "writeOnly":
          writeOnly = booleanJsonAdapter.fromJson(reader);
          break;
        case "externalDocumentation":
          externalDocs = externalDocumentationJsonAdapter.fromJson(reader);
          break;
        case "deprecated":
          deprecated = booleanJsonAdapter.fromJson(reader);
          break;
        case "xml":
          xml = xmlJsonAdapter.fromJson(reader);
          break;
        case "extensions":
          extensions = mapStringObjectJsonAdaptor.fromJson(reader);
          break;
        case "discriminator":
          discriminator = discriminatorJsonAdapter.fromJson(reader);
          break;
        case "exampleSetFlag":
          // Not needed - is based on the presence/absence of an "example"
          break;
        case "prefixItems":
          prefixItems = readListSelf(reader);
          break;
        case "allOf":
          allOf = readListSelf(reader);
          break;
        case "anyOf":
          anyOf = readListSelf(reader);
          break;
        case "oneOf":
          oneOf = readListSelf(reader);
          break;
        case "items":
          items = readSelf(reader);
          break;
        case "specVersion":
          reader.skipValue();
          break;
        case "types":
          types = setStringJsonAdaptor.fromJson(reader);
          break;
        case "patternProperties":
          patternProperties = readMapSelf(reader);
          break;
        case "exclusiveMaximumValue":
          exclusiveMaximumValue = bigDecimalJsonAdapter.fromJson(reader);
          break;
        case "exclusiveMinimumValue":
          exclusiveMinimumValue = bigDecimalJsonAdapter.fromJson(reader);
          break;
        case "contains":
          contains = readSelf(reader);
          break;
        case "$id":
          $id = stringJsonAdapter.fromJson(reader);
          break;
        case "$schema":
          $schema = stringJsonAdapter.fromJson(reader);
          break;
        case "$anchor":
          $anchor = stringJsonAdapter.fromJson(reader);
          break;
        case "$vocabulary":
          $vocabulary = stringJsonAdapter.fromJson(reader);
          break;
        case "$dynamicAnchor":
          $dynamicAnchor = stringJsonAdapter.fromJson(reader);
          break;
        case "$dynamicRef":
          $dynamicRef = stringJsonAdapter.fromJson(reader);
          break;
        case "contentEncoding":
          contentEncoding = stringJsonAdapter.fromJson(reader);
          break;
        case "contentMediaType":
          contentMediaType = stringJsonAdapter.fromJson(reader);
          break;
        case "contentSchema":
          contentSchema = readSelf(reader);
          break;
        case "propertyNames":
          propertyNames = readSelf(reader);
          break;
        case "unevaluatedProperties":
          unevaluatedProperties = readSelf(reader);
          break;
        case "maxContains":
          maxContains = integerJsonAdapter.fromJson(reader);
          break;
        case "minContains":
          minContains = integerJsonAdapter.fromJson(reader);
          break;
        case "additionalItems":
          additionalItems = readSelf(reader);
          break;
        case "unevaluatedItems":
          unevaluatedItems = readSelf(reader);
          break;
        case "if":
          _if = readSelf(reader);
          break;
        case "else":
          _else = readSelf(reader);
          break;
        case "then":
          then = readSelf(reader);
          break;
        case "dependentSchemas":
          dependentSchemas = readMapSelf(reader);
          break;
        case "dependentRequired":
          dependentRequired = mapStringListStringJsonAdaptor.fromJson(reader);
          break;
        case "$comment":
          $comment = stringJsonAdapter.fromJson(reader);
          break;
        case "booleanSchemaValue":
          booleanSchemaValue = booleanJsonAdapter.fromJson(reader);
          break;
        case "examples":
          examples = objectListJsonAdapter.fromJson(reader);
          break;
        case "enum":
          _enum = objectListJsonAdapter.fromJson(reader);
          break;
        case "example":
          example = objectJsonAdapter.fromJson(reader);
          break;
        case "const":
          _const = objectJsonAdapter.fromJson(reader);
          break;
        default:
          reader.unmappedField(fieldName);
          reader.skipValue();
      }
    }
    reader.endObject();
    final Set<String> normalizedTypes = normalizeTypes(type, types, nullable);
    final Schema schema = createSchemaFromInformation(schemaTypeForCreation(type, normalizedTypes), format);
    schema.name(name)
      .title(title)
      .multipleOf(multipleOf)
      .maximum(maximum)
      .exclusiveMaximum(exclusiveMaximum)
      .minimum(minimum)
      .exclusiveMinimum(exclusiveMinimum)
      .maxLength(maxLength)
      .minLength(minLength)
      .pattern(pattern)
      .maxItems(maxItems)
      .minItems(minItems)
      .uniqueItems(uniqueItems)
      .maxProperties(maxProperties)
      .minProperties(minProperties)
      .required(required)
      .not(not)
      .properties(properties)
      .additionalProperties(additionalProperties)
      .description(description)
      .$ref($ref)
      .nullable(null)
      .readOnly(readOnly)
      .writeOnly(writeOnly)
      .externalDocs(externalDocs)
      .deprecated(deprecated)
      .xml(xml)
      .extensions(extensions)
      .discriminator(discriminator)
      .prefixItems(prefixItems)
      .allOf(allOf)
      .anyOf(anyOf)
      .oneOf(oneOf)
      .items(items)
      .types(normalizedTypes)
      .patternProperties(patternProperties)
      .exclusiveMaximumValue(exclusiveMaximumValue)
      .exclusiveMinimumValue(exclusiveMinimumValue)
      .contains(contains)
      .$id($id)
      .$schema($schema)
      .$anchor($anchor)
      .$vocabulary($vocabulary)
      .$dynamicAnchor($dynamicAnchor)
      .$dynamicRef($dynamicRef)
      .contentEncoding(contentEncoding)
      .contentMediaType(contentMediaType)
      .contentSchema(contentSchema)
      .propertyNames(propertyNames)
      .unevaluatedProperties(unevaluatedProperties)
      .maxContains(maxContains)
      .minContains(minContains)
      .additionalItems(additionalItems)
      .unevaluatedItems(unevaluatedItems)
      ._if(_if)
      ._else(_else)
      .then(then)
      .dependentSchemas(dependentSchemas)
      .dependentRequired(dependentRequired)
      .$comment($comment)
      .booleanSchemaValue(booleanSchemaValue)
      .examples(examples)
      .example(example)
      ._enum(_enum)
      ._const(_const)
    ;
    return schema;
  }

  public static Schema createSchemaFromInformation(final String type, final String format) {
    if (type == null) {
      return new ArbitrarySchema();
    }
    switch (type) {
      case "array":
        return new ArraySchema().format(format);
      case "boolean":
        return new BooleanSchema().format(format);
      case "integer":
        return new IntegerSchema().format(format);
      case "object":
        return new ObjectSchema().format(format);
      case "number":
        return new NumberSchema().format(format);
      case "string":
        if (format == null) {
          return new StringSchema();
        }
        switch (format) {
          case "binary":
            return new BinarySchema();
          case "byte":
            return new ByteArraySchema();
          case "date":
            return new DateSchema();
          case "date-time":
            return new DateTimeSchema();
          case "email":
            return new EmailSchema();
          case "password":
            return new PasswordSchema();
          case "uuid":
            return new UUIDSchema();
          default:
            return new StringSchema().format(format);
        }
      default:
        return new ArbitrarySchema().format(format).type(type);
    }
  }

  private static String schemaTypeForCreation(final String type, final Set<String> types) {
    if (type != null && !type.isBlank()) {
      return type;
    }
    if (types == null || types.isEmpty()) {
      return null;
    }
    if (types.size() == 1) {
      return types.iterator().next();
    }
    for (final String candidate : types) {
      if (!"null".equals(candidate)) {
        return candidate;
      }
    }
    return null;
  }

  private static Set<String> normalizeTypes(
    final String type,
    final Set<String> types,
    final Boolean nullable) {

    final boolean hasTypeSet = types != null && !types.isEmpty();
    final boolean hasNullableUnion = Boolean.TRUE.equals(nullable);
    if (!hasTypeSet && !hasNullableUnion) {
      return null;
    }

    final LinkedHashSet<String> normalized = new LinkedHashSet<>();
    if (hasTypeSet) {
      for (final String candidate : types) {
        if (candidate != null && !candidate.isBlank() && !"null".equals(candidate)) {
          normalized.add(candidate);
        }
      }
    }
    if (type != null && !type.isBlank() && !"null".equals(type)) {
      normalized.add(type);
    }

    boolean includeNull = false;
    if (hasTypeSet && types.contains("null")) {
      includeNull = true;
    }
    if (Boolean.TRUE.equals(nullable)) {
      includeNull = true;
    } else if (Boolean.FALSE.equals(nullable)) {
      includeNull = false;
    }
    if (includeNull) {
      normalized.add("null");
    }
    return normalized.isEmpty() ? null : normalized;
  }

  private static String scalarSchemaType(final Set<String> normalizedTypes) {
    if (normalizedTypes == null || normalizedTypes.size() != 1) {
      return null;
    }
    return normalizedTypes.iterator().next();
  }

  private Schema readSelf(JsonReader reader) {
    if (reader.isNullValue()) {
      reader.skipValue();
      return null;
    }
    return fromJson(reader);
  }

  private Map<String, Schema> readMapSelf(JsonReader reader) {
    if (reader.isNullValue()) {
      reader.skipValue();
      return null;
    }
    final Map<String, Schema> result = new HashMap<>();
    reader.beginObject();
    while (reader.hasNextField()) {
      final String key = reader.nextField();
      if (reader.isNullValue()) {
        reader.skipValue();
        result.put(key, null);
      } else {
        result.put(key, fromJson(reader));
      }
    }
    reader.endObject();
    return result;
  }

  private List<Schema> readListSelf(JsonReader reader) {
    if (reader.isNullValue()) {
      reader.skipValue();
      return null;
    }
    final List<Schema> result = new ArrayList<>();
    reader.beginArray();
    while (reader.hasNextElement()) {
      result.add(readSelf(reader));
    }
    reader.endArray();
    return result;
  }

  private void toJsonImpl(JsonWriter writer, Schema<?> value) {
    writer.name(0);
    final Set<String> serializedTypes = normalizeTypes(value.getType(), value.getTypes(), value.getNullable());
    final String scalarType = scalarSchemaType(serializedTypes);
    if (scalarType != null) {
      stringJsonAdapter.toJson(writer, scalarType);
    } else if (serializedTypes != null && !serializedTypes.isEmpty()) {
      setStringJsonAdaptor.toJson(writer, serializedTypes);
    } else {
      stringJsonAdapter.toJson(writer, value.getType());
    }
    writer.name(1);
    stringJsonAdapter.toJson(writer, value.getFormat());
    writer.name(2);
    stringJsonAdapter.toJson(writer, value.getName());
    writer.name(3);
    stringJsonAdapter.toJson(writer, value.getTitle());
    writer.name(4);
    bigDecimalJsonAdapter.toJson(writer, value.getMultipleOf());
    writer.name(5);
    bigDecimalJsonAdapter.toJson(writer, value.getMaximum());
    writer.name(6);
    booleanJsonAdapter.toJson(writer, value.getExclusiveMaximum());
    writer.name(7);
    bigDecimalJsonAdapter.toJson(writer, value.getMinimum());
    writer.name(8);
    booleanJsonAdapter.toJson(writer, value.getExclusiveMinimum());
    writer.name(9);
    integerJsonAdapter.toJson(writer, value.getMaxLength());
    writer.name(10);
    integerJsonAdapter.toJson(writer, value.getMinLength());
    writer.name(11);
    stringJsonAdapter.toJson(writer, value.getPattern());
    writer.name(12);
    integerJsonAdapter.toJson(writer, value.getMaxItems());
    writer.name(13);
    integerJsonAdapter.toJson(writer, value.getMinItems());
    writer.name(14);
    booleanJsonAdapter.toJson(writer, value.getUniqueItems());
    writer.name(15);
    integerJsonAdapter.toJson(writer, value.getMaxProperties());
    writer.name(16);
    integerJsonAdapter.toJson(writer, value.getMinProperties());
    writer.name(17);
    listStringJsonAdaptor.toJson(writer, value.getRequired());
    writer.name(18);
    writeSelfNullSafe(value.getNot(), writer);
    writer.name(19);
    writeMapSelf(value.getProperties(), writer);
    writer.name(20);
    if (value.getAdditionalProperties() == null) {
      writer.nullValue();
    } else if (value.getAdditionalProperties() instanceof Boolean) {
      booleanJsonAdapter.toJson(writer, (Boolean) value.getAdditionalProperties());
    } else {
      writer.beginObject(names);
      toJsonImpl(writer, (Schema) value.getAdditionalProperties());
      writer.endObject();
    }
    writer.name(21);
    stringJsonAdapter.toJson(writer, value.getDescription());
    writer.name(22);
    stringJsonAdapter.toJson(writer, value.get$ref());
    writer.name(23);
    writer.nullValue();
    writer.name(24);
    booleanJsonAdapter.toJson(writer, value.getReadOnly());
    writer.name(25);
    booleanJsonAdapter.toJson(writer, value.getWriteOnly());
    writer.name(26);
    externalDocumentationJsonAdapter.toJson(writer, value.getExternalDocs());
    writer.name(27);
    booleanJsonAdapter.toJson(writer, value.getDeprecated());
    writer.name(28);
    xmlJsonAdapter.toJson(writer, value.getXml());
    writer.name(29);
    mapStringObjectJsonAdaptor.toJson(writer, value.getExtensions());
    writer.name(30);
    discriminatorJsonAdapter.toJson(writer, value.getDiscriminator());
    writer.name(31);
    if (value.getExample() != null) {
      booleanJsonAdapter.toJson(writer, Boolean.TRUE);
    } else {
      writer.nullValue();
    }
    writer.name(32);
    writeListSchema(writer, value.getPrefixItems());
    writer.name(33);
    writeListSchema(writer, value.getAllOf());
    writer.name(34);
    writeListSchema(writer, value.getAnyOf());
    writer.name(35);
    writeListSchema(writer, value.getOneOf());
    writer.name(36);
    writeSelfNullSafe(value.getItems(), writer);
    writer.name(37);
    // specVersion - ignored
    writer.nullValue();
    writer.name(38);
    writer.nullValue();
    writer.name(39);
    writeMapSelf(value.getPatternProperties(), writer);
    writer.name(40);
    bigDecimalJsonAdapter.toJson(writer, value.getExclusiveMaximumValue());
    writer.name(41);
    bigDecimalJsonAdapter.toJson(writer, value.getExclusiveMinimumValue());
    writer.name(42);
    writeSelfNullSafe(value.getContains(), writer);
    writer.name(43);
    stringJsonAdapter.toJson(writer, value.get$id());
    writer.name(44);
    stringJsonAdapter.toJson(writer, value.get$schema());
    writer.name(45);
    stringJsonAdapter.toJson(writer, value.get$anchor());
    writer.name(46);
    stringJsonAdapter.toJson(writer, value.get$vocabulary());
    writer.name(47);
    stringJsonAdapter.toJson(writer, value.get$dynamicAnchor());
    writer.name(48);
    stringJsonAdapter.toJson(writer, value.get$dynamicRef());
    writer.name(49);
    stringJsonAdapter.toJson(writer, value.getContentEncoding());
    writer.name(50);
    stringJsonAdapter.toJson(writer, value.getContentMediaType());
    writer.name(51);
    writeSelfNullSafe(value.getContentSchema(), writer);
    writer.name(52);
    writeSelfNullSafe(value.getPropertyNames(), writer);
    writer.name(53);
    writeSelfNullSafe(value.getUnevaluatedProperties(), writer);
    writer.name(54);
    integerJsonAdapter.toJson(writer, value.getMaxContains());
    writer.name(55);
    integerJsonAdapter.toJson(writer, value.getMinContains());
    writer.name(56);
    writeSelfNullSafe(value.getAdditionalItems(), writer);
    writer.name(57);
    writeSelfNullSafe(value.getUnevaluatedItems(), writer);
    writer.name(58);
    writeSelfNullSafe(value.getIf(), writer);
    writer.name(59);
    writeSelfNullSafe(value.getElse(), writer);
    writer.name(60);
    writeSelfNullSafe(value.getThen(), writer);
    writer.name(61);
    writeMapSelf(value.getDependentSchemas(), writer);
    writer.name(62);
    mapStringListStringJsonAdaptor.toJson(writer, value.getDependentRequired());
    writer.name(63);
    stringJsonAdapter.toJson(writer, value.get$comment());
    writer.name(64);
    booleanJsonAdapter.toJson(writer, value.getBooleanSchemaValue());
    // OK, now to try and continue!
    if (value instanceof BinarySchema) {
      toJsonSchemaByteArray(writer, (BinarySchema) value);
    } else if (value instanceof BooleanSchema) {
      toJsonSchemaBoolean(writer, (BooleanSchema) value);
    } else if (value instanceof ByteArraySchema) {
      toJsonSchemaByteArray(writer, (ByteArraySchema) value);
    } else if (value instanceof DateSchema) {
      toJsonSchemaDate(writer, (DateSchema) value);
    } else if (value instanceof DateTimeSchema) {
      toJsonSchemaOdt(writer, (DateTimeSchema) value);
    } else if (value instanceof EmailSchema) {
      toJsonSchemaString(writer, (EmailSchema) value);
    } else if (value instanceof FileSchema) {
      toJsonSchemaString(writer, (FileSchema) value);
    } else if (value instanceof IntegerSchema) {
      toJsonSchemaNumber(writer, (IntegerSchema) value);
    } else if (value instanceof NumberSchema) {
      toJsonSchemaBigDecimal(writer, (NumberSchema) value);
    } else if (value instanceof PasswordSchema) {
      toJsonSchemaString(writer, (PasswordSchema) value);
    } else if (value instanceof StringSchema) {
      toJsonSchemaString(writer, (StringSchema) value);
    } else if (value instanceof UUIDSchema) {
      toJsonSchemaUuid(writer, (UUIDSchema) value);
    }
  }

  private void toJsonSchemaByteArray(final JsonWriter writer, final Schema<byte[]> value) {
    // examples, example, enum, const
    writer.name(65);
    listByteArrayJsonAdaptor.toJson(writer, value.getExamples());
    writer.name(66);
    byteArrayJsonAdaptor.toJson(writer, (byte[]) value.getExample());
    writer.name(67);
    listByteArrayJsonAdaptor.toJson(writer, value.getEnum());
    writer.name(68);
    byteArrayJsonAdaptor.toJson(writer, value.getConst());
  }

  private void toJsonSchemaBoolean(final JsonWriter writer, final Schema<Boolean> value) {
    // examples, example, enum, const
    writer.name(65);
    listBooleanAdaptor.toJson(writer, value.getExamples());
    writer.name(66);
    booleanJsonAdapter.toJson(writer, (Boolean) value.getExample());
    writer.name(67);
    listBooleanAdaptor.toJson(writer, value.getEnum());
    writer.name(68);
    booleanJsonAdapter.toJson(writer, value.getConst());
  }

  private void toJsonSchemaDate(final JsonWriter writer, final Schema<Date> value) {
    // examples, example, enum, const
    writer.name(65);
    listDateJsonAdaptor.toJson(writer, value.getExamples());
    writer.name(66);
    dateJsonAdaptor.toJson(writer, (Date) value.getExample());
    writer.name(67);
    listDateJsonAdaptor.toJson(writer, value.getEnum());
    writer.name(68);
    dateJsonAdaptor.toJson(writer, value.getConst());
  }

  private void toJsonSchemaOdt(final JsonWriter writer, final Schema<OffsetDateTime> value) {
    // examples, example, enum, const
    writer.name(65);
    listOdtJsonAdaptor.toJson(writer, value.getExamples());
    writer.name(66);
    odtJsonAdaptor.toJson(writer, (OffsetDateTime) value.getExample());
    writer.name(67);
    listOdtJsonAdaptor.toJson(writer, value.getEnum());
    writer.name(68);
    odtJsonAdaptor.toJson(writer, value.getConst());
  }

  private void toJsonSchemaString(final JsonWriter writer, final Schema<String> value) {
    // examples, example, enum, const
    writer.name(65);
    listStringJsonAdaptor.toJson(writer, value.getExamples());
    writer.name(66);
    stringJsonAdapter.toJson(writer, (String) value.getExample());
    writer.name(67);
    listStringJsonAdaptor.toJson(writer, value.getEnum());
    writer.name(68);
    stringJsonAdapter.toJson(writer, value.getConst());
  }

  private void toJsonSchemaNumber(final JsonWriter writer, final Schema<Number> value) {
    // examples, example, enum, const
    writer.name(65);
    if(value.getExamples() == null) {
      writer.nullValue();
    } else {
      listNumberJsonAdaptor.toJson(writer, value.getExamples()
        .stream()
        .map(Number::toString)
        .map(BigDecimal::new)
        .collect(Collectors.toList())
      );
    }
    writer.name(66);
    if (value.getExample() == null) {
      writer.nullValue();
    } else {
      numberJsonAdapter.toJson(writer, new BigDecimal(value.getExample().toString()));
    }
    writer.name(67);
    if(value.getEnum() == null) {
      writer.nullValue();
    } else {
      listNumberJsonAdaptor.toJson(writer, value.getEnum()
        .stream()
        .map(Number::toString)
        .map(BigDecimal::new)
        .collect(Collectors.toList())
      );
    }
    writer.name(68);
    if (value.getConst() == null) {
      writer.nullValue();
    } else {
      numberJsonAdapter.toJson(writer, new BigDecimal(value.getConst().toString()));
    }
  }

  private void toJsonSchemaBigDecimal(final JsonWriter writer, final Schema<BigDecimal> value) {
    // examples, example, enum, const
    writer.name(65);
    listBigDecimalJsonAdaptor.toJson(writer, value.getExamples());
    writer.name(66);
    bigDecimalJsonAdapter.toJson(writer, (BigDecimal) value.getExample());
    writer.name(67);
    listBigDecimalJsonAdaptor.toJson(writer, value.getEnum());
    writer.name(68);
    bigDecimalJsonAdapter.toJson(writer, value.getConst());
  }

  private void toJsonSchemaUuid(final JsonWriter writer, final Schema<UUID> value) {
    // examples, example, enum, const
    writer.name(65);
    listUuidJsonAdaptor.toJson(writer, value.getExamples());
    writer.name(66);
    uuidJsonAdapter.toJson(writer, (UUID) value.getExample());
    writer.name(67);
    listUuidJsonAdaptor.toJson(writer, value.getEnum());
    writer.name(68);
    uuidJsonAdapter.toJson(writer, value.getConst());
  }

  private void writeMapSelf(final Map<String, Schema> value, final JsonWriter writer) {
    if (value == null || value.isEmpty()) {
      writer.nullValue();
    } else {
      writer.beginObject();
      for (final Map.Entry<String, Schema> stringSchemaEntry : value.entrySet()) {
        writer.name(stringSchemaEntry.getKey());
        writeSelfNullSafe(stringSchemaEntry.getValue(), writer);
      }
      writer.endObject();
    }
  }

  private void writeSelfNullSafe(final Schema value, final JsonWriter writer) {
    if (value == null) {
      writer.nullValue();
    } else {
      writer.beginObject(names);
      toJsonImpl(writer, value);
      writer.endObject();
    }
  }

  private void writeListSchema(final JsonWriter writer, final List<Schema> schemas) {
    if (schemas == null) {
      writer.nullValue();
    } else if (schemas.isEmpty()) {
      writer.emptyArray();
    } else {
      writer.beginArray();
      for (final Schema<?> schema : schemas) {
        writer.beginObject(names);
        toJsonImpl(writer, schema);
        writer.endObject();
      }
      writer.endArray();
    }
  }
}
