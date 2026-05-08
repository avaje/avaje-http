package io.avaje.http.openapi;

import io.swagger.v3.oas.models.media.ArbitrarySchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.EmailSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.PasswordSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Shared OpenAPI 3.1 schema normalization helpers. */
public final class OpenApiSchemaNormalizer {

  private static final String NULL_TYPE = "null";

  private OpenApiSchemaNormalizer() {}

  /** Mark a schema as allowing JSON null using OpenAPI 3.1 / JSON Schema syntax. */
  public static Schema<?> nullable(Schema<?> schema) {
    if (schema == null) {
      return null;
    }
    if (allowsNull(schema)) {
      schema.setNullable(null);
      return schema;
    }
    if (isReferenceOrComposite(schema)) {
      return nullableWrapper(schema);
    }
    final var types = effectiveTypes(schema);
    if (types.isEmpty()) {
      return nullableWrapper(schema);
    }
    types.add(NULL_TYPE);
    applyTypes(schema, types);
    schema.setNullable(null);
    return schema;
  }

  /** Mark a schema as not allowing JSON null. */
  public static Schema<?> notNullable(Schema<?> schema) {
    if (schema == null) {
      return null;
    }
    schema.setNullable(null);
    if (schema.getTypes() != null && schema.getTypes().contains(NULL_TYPE)) {
      final var types = new LinkedHashSet<String>(schema.getTypes());
      types.remove(NULL_TYPE);
      applyTypes(schema, types);
    }
    schema.setAnyOf(withoutNullSchema(schema.getAnyOf()));
    schema.setOneOf(withoutNullSchema(schema.getOneOf()));
    return unwrapSingleAnyOfOrOneOf(schema);
  }

  /** Normalize legacy nullable fields and nested schemas to OpenAPI 3.1 form. */
  public static Schema<?> normalize(Schema<?> schema) {
    if (schema == null) {
      return null;
    }
    normalizeNested(schema);
    final Boolean nullable = schema.getNullable();
    if (Boolean.TRUE.equals(nullable)) {
      return nullable(schema);
    }
    if (Boolean.FALSE.equals(nullable)) {
      return notNullable(schema);
    }
    normalizeTypeFields(schema);
    return schema;
  }

  /** Effective non-null first type from a schema. */
  public static String firstNonNullType(Schema<?> schema) {
    if (schema == null) {
      return null;
    }
    final var type = schema.getType();
    if (isType(type)) {
      return type;
    }
    if (schema.getTypes() != null) {
      for (final String candidate : schema.getTypes()) {
        if (isType(candidate)) {
          return candidate;
        }
      }
    }
    return null;
  }

  /** Normalize schema type data, including legacy nullable, to a type set. */
  public static Set<String> normalizeTypes(
      final String type, final Set<String> types, final Boolean nullable) {

    final var normalized = effectiveTypes(type, types);
    if (Boolean.TRUE.equals(nullable)) {
      normalized.add(NULL_TYPE);
    } else if (Boolean.FALSE.equals(nullable)) {
      normalized.remove(NULL_TYPE);
    }
    return normalized.isEmpty() ? null : normalized;
  }

  /** Type to use when picking a concrete Swagger schema subclass. */
  public static String schemaTypeForCreation(final String type, final Set<String> types) {
    if (isType(type)) {
      return type;
    }
    if (types == null || types.isEmpty()) {
      return null;
    }
    for (final String candidate : types) {
      if (isType(candidate)) {
        return candidate;
      }
    }
    return null;
  }

  /** Return the scalar type when the normalized set contains exactly one type. */
  public static String scalarSchemaType(final Set<String> normalizedTypes) {
    if (normalizedTypes == null || normalizedTypes.size() != 1) {
      return null;
    }
    return normalizedTypes.iterator().next();
  }

  /** Return the JSON value for a Schema type keyword. */
  public static Object schemaTypeValue(Schema<?> schema) {
    final var types = normalizeTypes(schema.getType(), schema.getTypes(), schema.getNullable());
    if (types == null || types.isEmpty()) {
      return null;
    }
    if (types.size() == 1) {
      return types.iterator().next();
    }
    return types;
  }

  /** True when schema has an OpenAPI/JSON Schema type set. */
  public static boolean hasTypeSet(Schema<?> schema) {
    return schema != null && schema.getTypes() != null && !schema.getTypes().isEmpty();
  }

  /** Whether the schema has explicit type/nullability information for merge precedence. */
  public static boolean hasTypeInformation(Schema<?> schema) {
    return schema != null
        && (isType(schema.getType())
            || (schema.getTypes() != null && !schema.getTypes().isEmpty())
            || schema.getNullable() != null);
  }

  /** Effective type set for merging. Includes scalar type values. */
  public static Set<String> effectiveTypesForMerge(Schema<?> schema) {
    if (schema == null) {
      return null;
    }
    final var normalized = normalizeTypes(schema.getType(), schema.getTypes(), schema.getNullable());
    return normalized == null || normalized.isEmpty() ? null : normalized;
  }

  /** Create the Swagger schema subclass matching an OpenAPI type and format. */
  public static Schema<?> createSchemaFromInformation(final String type, final String format) {
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
        return stringSchema(format);
      default:
        return new ArbitrarySchema().format(format).type(type);
    }
  }

  /** Numeric exclusiveMaximum value for OpenAPI 3.1 output. */
  public static BigDecimal exclusiveMaximumValue(Schema<?> schema) {
    if (schema == null) {
      return null;
    }
    final var value = schema.getExclusiveMaximumValue();
    if (value != null) {
      return value;
    }
    return Boolean.TRUE.equals(schema.getExclusiveMaximum()) ? schema.getMaximum() : null;
  }

  /** Numeric exclusiveMinimum value for OpenAPI 3.1 output. */
  public static BigDecimal exclusiveMinimumValue(Schema<?> schema) {
    if (schema == null) {
      return null;
    }
    final var value = schema.getExclusiveMinimumValue();
    if (value != null) {
      return value;
    }
    return Boolean.TRUE.equals(schema.getExclusiveMinimum()) ? schema.getMinimum() : null;
  }

  /** Maximum should be omitted when a legacy exclusiveMaximum boolean consumed it. */
  public static boolean omitMaximum(Schema<?> schema) {
    return schema != null
        && schema.getExclusiveMaximumValue() == null
        && Boolean.TRUE.equals(schema.getExclusiveMaximum())
        && schema.getMaximum() != null;
  }

  /** Minimum should be omitted when a legacy exclusiveMinimum boolean consumed it. */
  public static boolean omitMinimum(Schema<?> schema) {
    return schema != null
        && schema.getExclusiveMinimumValue() == null
        && Boolean.TRUE.equals(schema.getExclusiveMinimum())
        && schema.getMinimum() != null;
  }

  private static Schema<?> stringSchema(String format) {
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
  }

  private static void normalizeNested(Schema<?> schema) {
    schema.setNot(normalize(schema.getNot()));
    schema.setItems(normalize(schema.getItems()));
    schema.setContains(normalize(schema.getContains()));
    schema.setContentSchema(normalize(schema.getContentSchema()));
    schema.setPropertyNames(normalize(schema.getPropertyNames()));
    schema.setUnevaluatedProperties(normalize(schema.getUnevaluatedProperties()));
    schema.setAdditionalItems(normalize(schema.getAdditionalItems()));
    schema.setUnevaluatedItems(normalize(schema.getUnevaluatedItems()));
    schema.setIf(normalize(schema.getIf()));
    schema.setElse(normalize(schema.getElse()));
    schema.setThen(normalize(schema.getThen()));
    schema.setProperties(normalizeMap(schema.getProperties()));
    schema.setPatternProperties(normalizeMap(schema.getPatternProperties()));
    schema.setDependentSchemas(normalizeMap(schema.getDependentSchemas()));
    schema.setAdditionalProperties(normalizeAdditionalProperties(schema.getAdditionalProperties()));
    schema.setPrefixItems(normalizeList(schema.getPrefixItems()));
    schema.setAllOf(normalizeList(schema.getAllOf()));
    schema.setAnyOf(normalizeList(schema.getAnyOf()));
    schema.setOneOf(normalizeList(schema.getOneOf()));
  }

  private static Map<String, Schema> normalizeMap(Map<String, Schema> schemas) {
    if (schemas == null) {
      return null;
    }
    schemas.replaceAll((key, schema) -> (Schema) normalize(schema));
    return schemas;
  }

  private static Object normalizeAdditionalProperties(Object additionalProperties) {
    if (additionalProperties instanceof Schema<?>) {
      return normalize((Schema<?>) additionalProperties);
    }
    return additionalProperties;
  }

  private static List<Schema> normalizeList(List<Schema> schemas) {
    if (schemas == null) {
      return null;
    }
    for (int i = 0; i < schemas.size(); i++) {
      schemas.set(i, (Schema) normalize(schemas.get(i)));
    }
    return schemas;
  }

  private static void normalizeTypeFields(Schema<?> schema) {
    final var types = normalizeTypes(schema.getType(), schema.getTypes(), null);
    if (types != null && !types.isEmpty()) {
      applyTypes(schema, types);
    }
    schema.setNullable(null);
  }

  private static LinkedHashSet<String> effectiveTypes(Schema<?> schema) {
    return effectiveTypes(schema.getType(), schema.getTypes());
  }

  private static LinkedHashSet<String> effectiveTypes(String type, Set<String> types) {
    final var normalized = new LinkedHashSet<String>();
    if (isType(type)) {
      normalized.add(type);
    } else if (NULL_TYPE.equals(type)) {
      normalized.add(NULL_TYPE);
    }
    if (types != null) {
      for (final String candidate : types) {
        if (isType(candidate) || NULL_TYPE.equals(candidate)) {
          normalized.add(candidate);
        }
      }
    }
    return normalized;
  }

  private static void applyTypes(Schema<?> schema, Set<String> types) {
    if (types == null || types.isEmpty()) {
      schema.setTypes(null);
      return;
    }
    schema.setType(null);
    schema.setTypes(new LinkedHashSet<>(types));
  }

  private static boolean isType(String type) {
    return type != null && !type.isBlank() && !NULL_TYPE.equals(type);
  }

  private static boolean isReferenceOrComposite(Schema<?> schema) {
    return schema.get$ref() != null
        || notEmpty(schema.getAllOf())
        || notEmpty(schema.getAnyOf())
        || notEmpty(schema.getOneOf())
        || schema.getNot() != null;
  }

  private static boolean notEmpty(List<?> value) {
    return value != null && !value.isEmpty();
  }

  private static boolean allowsNull(Schema<?> schema) {
    return NULL_TYPE.equals(schema.getType())
        || (schema.getTypes() != null && schema.getTypes().contains(NULL_TYPE))
        || hasNullSchema(schema.getAnyOf())
        || hasNullSchema(schema.getOneOf());
  }

  private static Schema<?> nullableWrapper(Schema<?> schema) {
    final var anyOf = new ArrayList<Schema>();
    schema.setNullable(null);
    anyOf.add(schema);
    anyOf.add(new Schema<>().type(NULL_TYPE));
    return new Schema<>().anyOf(anyOf);
  }

  private static boolean hasNullSchema(List<Schema> schemas) {
    return schemas != null && schemas.stream().anyMatch(OpenApiSchemaNormalizer::isNullOnlySchema);
  }

  private static List<Schema> withoutNullSchema(List<Schema> schemas) {
    if (schemas == null) {
      return null;
    }
    schemas.removeIf(OpenApiSchemaNormalizer::isNullOnlySchema);
    return schemas.isEmpty() ? null : schemas;
  }

  private static boolean isNullOnlySchema(Schema<?> schema) {
    if (schema == null) {
      return false;
    }
    if (NULL_TYPE.equals(schema.getType())) {
      return true;
    }
    final var types = schema.getTypes();
    return types != null && types.size() == 1 && types.contains(NULL_TYPE);
  }

  private static Schema<?> unwrapSingleAnyOfOrOneOf(Schema<?> schema) {
    if (schema.getAnyOf() != null && schema.getAnyOf().size() == 1 && schema.getOneOf() == null) {
      return schema.getAnyOf().get(0);
    }
    if (schema.getOneOf() != null && schema.getOneOf().size() == 1 && schema.getAnyOf() == null) {
      return schema.getOneOf().get(0);
    }
    return schema;
  }
}
