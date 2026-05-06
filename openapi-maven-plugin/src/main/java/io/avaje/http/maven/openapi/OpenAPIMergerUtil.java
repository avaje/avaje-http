package io.avaje.http.maven.openapi;

import io.avaje.http.maven.openapi.jsonb.SchemaCustomAdaptor;
import io.avaje.jsonb.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.XML;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility for merging instances of {@link OpenAPI} together.
 */
@Json.Import({OpenAPI.class, MediaType.class, Discriminator.class, XML.class})
final class OpenAPIMergerUtil {

  private OpenAPIMergerUtil() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Merge all provided instances of {@link OpenAPI}, with the first ones being considered the
   *   "preferred" source of truth for information, followed by the second, and so on.
   * @param apis the APIs to merge
   * @return the merged result, or null if there are no APIs to merge
   */
  public static OpenAPI merge(final OpenAPI ... apis) {
    if (apis == null || apis.length == 0) {
      return null;
    } else if (apis.length == 1) {
      return apis[0];
    } else {
      OpenAPI merged = apis[0];
      for (int i = 1; i < apis.length; i++) {
        merged = merge(merged, apis[i]);
      }
      return merged;
    }
  }

  /**
   * Merge two instances of {@link OpenAPI} together, preferring information in the "primary" source
   *   over that in the secondary
   * @param primary the primary source of information for the resulting API
   * @param secondary the secondary source of information for the resulting API
   * @return the two definitions merged into one
   */
  public static OpenAPI merge(final OpenAPI primary, final OpenAPI secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final OpenAPI merged = new OpenAPI();
      merged.setOpenapi(preferredOpenApiVersion(primary.getOpenapi(), secondary.getOpenapi()));
      merged.setInfo(merge(primary.getInfo(), secondary.getInfo()));
      merged.setExternalDocs(merge(primary.getExternalDocs(), secondary.getExternalDocs()));
      merged.setServers(merge(primary.getServers(), secondary.getServers(), Server::getUrl));
      merged.setSecurity(merge(primary.getSecurity(), secondary.getSecurity()));
      merged.setTags(merge(primary.getTags(), secondary.getTags(), Tag::getName, OpenAPIMergerUtil::merge));
      merged.setPaths(merge(primary.getPaths(), secondary.getPaths()));
      merged.setComponents(merge(primary.getComponents(), secondary.getComponents()));
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      merged.setJsonSchemaDialect(firstNotBlank(primary.getJsonSchemaDialect(), secondary.getJsonSchemaDialect()));
      merged.setSpecVersion(preferredSpecVersion(primary.getSpecVersion(), secondary.getSpecVersion()));
      return merged;
    }
  }

  private static Info merge(final Info primary, final Info secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Info merged = new Info();
      merged.setTitle(firstNotBlank(primary.getTitle(), secondary.getTitle()));
      merged.setDescription(firstNotBlank(primary.getDescription(), secondary.getDescription()));
      merged.setTermsOfService(firstNotBlank(primary.getTermsOfService(), secondary.getTermsOfService()));
      merged.setContact(merge(primary.getContact(), secondary.getContact()));
      merged.setLicense(merge(primary.getLicense(), secondary.getLicense()));
      merged.setVersion(firstNotBlank(primary.getVersion(), secondary.getVersion()));
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      merged.setSummary(firstNotBlank(primary.getSummary(), secondary.getSummary()));
      return merged;
    }
  }

  private static String firstNotBlank(final String primary, final String secondary) {
    if (primary != null && !(primary.isBlank())) {
      return primary;
    } else {
      return secondary;
    }
  }

  private static <T> T firstNonNull(final T primary, final T secondary) {
    if (primary == null) {
      return secondary;
    }
    return primary;
  }

  private static String preferredOpenApiVersion(final String primary, final String secondary) {
    final var primaryVersion = firstNotBlank(primary, null);
    final var secondaryVersion = firstNotBlank(secondary, null);
    if (primaryVersion == null) {
      return secondaryVersion;
    }
    if (secondaryVersion == null) {
      return primaryVersion;
    }
    final int primaryRank = openApiVersionRank(primaryVersion);
    final int secondaryRank = openApiVersionRank(secondaryVersion);
    if (secondaryRank > primaryRank) {
      return secondaryVersion;
    }
    return primaryVersion;
  }

  private static int openApiVersionRank(final String version) {
    final var normalized = version.trim();
    final var parts = normalized.split("\\.");
    if (parts.length < 2) {
      return 0;
    }
    try {
      return Integer.parseInt(parts[0]) * 100 + Integer.parseInt(parts[1]);
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private static SpecVersion preferredSpecVersion(final SpecVersion primary, final SpecVersion secondary) {
    if (primary == SpecVersion.V31 || secondary == SpecVersion.V31) {
      return SpecVersion.V31;
    }
    if (primary == SpecVersion.V30 || secondary == SpecVersion.V30) {
      return SpecVersion.V30;
    }
    return firstNonNull(primary, secondary);
  }

  private static String preferredSchemaType(final Schema primary, final Schema secondary) {
    final var explicitType = firstNotBlank(primary.getType(), secondary.getType());
    if (explicitType != null) {
      return explicitType;
    }
    final var primaryType = firstSchemaType(primary.getTypes());
    final var secondaryType = firstSchemaType(secondary.getTypes());
    return firstNotBlank(primaryType, secondaryType);
  }

  private static String firstSchemaType(final Set<String> types) {
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

  private static Set<String> mergedSchemaTypes(
    final Schema primary,
    final Schema secondary,
    final String preferredType) {

    final boolean hasTypeSet =
      (primary.getTypes() != null && !primary.getTypes().isEmpty())
        || (secondary.getTypes() != null && !secondary.getTypes().isEmpty());
    final Boolean nullable = firstNonNull(primary.getNullable(), secondary.getNullable());
    if (!hasTypeSet && !Boolean.TRUE.equals(nullable)) {
      return null;
    }

    final LinkedHashSet<String> merged = new LinkedHashSet<>();
    if (preferredType != null && !preferredType.isBlank() && !"null".equals(preferredType)) {
      merged.add(preferredType);
    }
    addSchemaTypes(merged, primary.getTypes());
    addSchemaTypes(merged, secondary.getTypes());
    addSchemaType(merged, primary.getType());
    addSchemaType(merged, secondary.getType());

    boolean includeNull =
      (primary.getTypes() != null && primary.getTypes().contains("null"))
        || (secondary.getTypes() != null && secondary.getTypes().contains("null"));
    if (Boolean.TRUE.equals(nullable)) {
      includeNull = true;
    } else if (Boolean.FALSE.equals(nullable)) {
      includeNull = false;
    }
    if (includeNull) {
      merged.add("null");
    }

    return merged.isEmpty() ? null : merged;
  }

  private static void addSchemaTypes(final Set<String> target, final Set<String> source) {
    if (source == null || source.isEmpty()) {
      return;
    }
    for (final String candidate : source) {
      addSchemaType(target, candidate);
    }
  }

  private static void addSchemaType(final Set<String> target, final String candidate) {
    if (candidate == null || candidate.isBlank() || "null".equals(candidate)) {
      return;
    }
    target.add(candidate);
  }

  private static <T, U> Map<T, U> merge(final Map<T, U> primary, final Map<T, U> secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Map<T, U> merged = new HashMap<>(primary);
      secondary.forEach(merged::putIfAbsent);
      return merged;
    }
  }

  private static <T, U> Map<T, U> merge(final Map<T, U> primary, final Map<T, U> secondary, final BiFunction<U, U, U> mergeInstances, final Class<U> clz) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Map<T, U> merged = new HashMap<>(primary);
      secondary.forEach((final T key, final U value) -> {
        if (merged.containsKey(key)) {
          merged.put(key, mergeInstances.apply(merged.get(key), value));
        } else {
          merged.put(key, value);
        }
      });
      return merged;
    }
  }

  private static <T, U> Map<T, U> merge(final Map<T, U> primary, final Map<T, U> secondary, final BiFunction<U, U, U> mergeInstances) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Map<T, U> merged = new HashMap<>(primary);
      secondary.forEach((final T key, final U value) -> {
        if (merged.containsKey(key)) {
          merged.put(key, mergeInstances.apply(merged.get(key), value));
        } else {
          merged.put(key, value);
        }
      });
      return merged;
    }
  }

  private static <T> List<T> merge(final List<T> primary, final List<T> secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final List<T> merged = new ArrayList<>(primary);
      merged.addAll(secondary);
      return merged;
    }
  }

  private static <T> List<T> merge(final List<T> primary, final List<T> secondary, final Function<T, String> uniqueKey) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Map<String, T> merged = new HashMap<>();
      for (final T t : primary) {
        final String key = uniqueKey.apply(t);
        merged.putIfAbsent(key, t);
      }
      for (final T t : secondary) {
        final String key = uniqueKey.apply(t);
        merged.putIfAbsent(key, t);
      }
      return new ArrayList<>(merged.values());
    }
  }

  private static <T> List<T> merge(final List<T> primary, final List<T> secondary, final Function<T, String> uniqueKey, final BiFunction<T, T, T> mergeMethod) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Map<String, T> merged = new HashMap<>();
      for (final T t : primary) {
        final String key = uniqueKey.apply(t);
        merged.putIfAbsent(key, t);
      }
      for (final T t : secondary) {
        final String key = uniqueKey.apply(t);
        if (merged.containsKey(key)) {
          merged.put(key, mergeMethod.apply(merged.get(key), t));
        } else {
          merged.put(key, t);
        }
      }
      return new ArrayList<>(merged.values());
    }
  }

  private static Contact merge(final Contact primary, final Contact secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Contact merged = new Contact();
      merged.setName(firstNotBlank(primary.getName(), secondary.getName()));
      merged.setUrl(firstNotBlank(primary.getUrl(), secondary.getUrl()));
      merged.setEmail(firstNotBlank(primary.getEmail(), secondary.getEmail()));
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      return merged;
    }
  }

  private static License merge(final License primary, final License secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final License merged = new License();
      merged.setName(firstNotBlank(primary.getName(), secondary.getName()));
      merged.setUrl(firstNotBlank(primary.getUrl(), secondary.getUrl()));
      merged.setIdentifier(firstNotBlank(primary.getIdentifier(), secondary.getIdentifier()));
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      return merged;
    }
  }

  private static ExternalDocumentation merge(final ExternalDocumentation primary, final ExternalDocumentation secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final ExternalDocumentation merged = new ExternalDocumentation();
      merged.setDescription(firstNotBlank(primary.getDescription(), secondary.getDescription()));
      merged.setUrl(firstNotBlank(primary.getUrl(), secondary.getUrl()));
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      return merged;
    }
  }

  private static Paths merge(final Paths primary, final Paths secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Paths merged = new Paths();
      merged.putAll(primary);
      secondary.forEach((final String key, final PathItem value) -> {
        if (merged.containsKey(key)) {
          merged.put(key, merge(merged.get(key), value));
        } else {
          merged.put(key, value);
        }
      });
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      return merged;
    }
  }

  private static PathItem merge(final PathItem primary, final PathItem secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final PathItem merged = new PathItem();
      merged.setSummary(firstNotBlank(primary.getSummary(), secondary.getSummary()));
      merged.setDescription(firstNotBlank(primary.getDescription(), secondary.getDescription()));
      // We could merge these... but then, defining multiple "get" and "put" etc. seems... fishy
      merged.setGet(firstNonNull(primary.getGet(), secondary.getGet()));
      merged.setPut(firstNonNull(primary.getPut(), secondary.getPut()));
      merged.setPost(firstNonNull(primary.getPost(), secondary.getPost()));
      merged.setDelete(firstNonNull(primary.getDelete(), secondary.getDelete()));
      merged.setOptions(firstNonNull(primary.getOptions(), secondary.getOptions()));
      merged.setHead(firstNonNull(primary.getHead(), secondary.getHead()));
      merged.setPatch(firstNonNull(primary.getPatch(), secondary.getPatch()));
      merged.setTrace(firstNonNull(primary.getTrace(), secondary.getTrace()));
      merged.setServers(merge(primary.getServers(), secondary.getServers(), Server::getUrl));
      merged.setParameters(merge(primary.getParameters(), secondary.getParameters(), Parameter::getName));
      merged.set$ref(firstNotBlank(primary.get$ref(), secondary.get$ref()));
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      return merged;
    }
  }

  private static Components merge(final Components primary, final Components secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Components merged = new Components();
      merged.setSchemas(merge(primary.getSchemas(), secondary.getSchemas(), OpenAPIMergerUtil::merge));
      merged.setResponses(merge(primary.getResponses(), secondary.getResponses(), OpenAPIMergerUtil::merge));
      merged.setParameters(merge(primary.getParameters(), secondary.getParameters()));
      merged.setExamples(merge(primary.getExamples(), secondary.getExamples()));
      merged.setRequestBodies(merge(primary.getRequestBodies(), secondary.getRequestBodies()));
      merged.setHeaders(merge(primary.getHeaders(), secondary.getHeaders()));
      merged.setSecuritySchemes(merge(primary.getSecuritySchemes(), secondary.getSecuritySchemes()));
      merged.setLinks(merge(primary.getLinks(), secondary.getLinks()));
      merged.setCallbacks(merge(primary.getCallbacks(), secondary.getCallbacks()));
      merged.setPathItems(merge(primary.getPathItems(), secondary.getPathItems()));
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      return merged;
    }
  }

  private static Schema merge(final Schema primary, final Schema secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      // We will alter "primary" to match
      final String type = preferredSchemaType(primary, secondary);
      final Set<String> types = mergedSchemaTypes(primary, secondary, type);
      final String format = firstNotBlank(primary.getFormat(), secondary.getFormat());
      return SchemaCustomAdaptor.createSchemaFromInformation(type, format)
        .type(type)
        .format(format)
        .name(firstNotBlank(primary.getName(), secondary.getName()))
        .title(firstNotBlank(primary.getTitle(), secondary.getTitle()))
        .multipleOf(firstNonNull(primary.getMultipleOf(), secondary.getMultipleOf()))
        .maximum(firstNonNull(primary.getMaximum(), secondary.getMaximum()))
        .exclusiveMaximum(firstNonNull(primary.getExclusiveMaximum(), secondary.getExclusiveMaximum()))
        .minimum(firstNonNull(primary.getMinimum(), secondary.getMinimum()))
        .exclusiveMinimum(firstNonNull(primary.getExclusiveMinimum(), secondary.getExclusiveMinimum()))
        .maxLength(firstNonNull(primary.getMaxLength(), secondary.getMaxLength()))
        .minLength(firstNonNull(primary.getMinLength(), secondary.getMinLength()))
        .pattern(firstNotBlank(primary.getPattern(), secondary.getPattern()))
        .maxItems(firstNonNull(primary.getMaxItems(), secondary.getMaxItems()))
        .minItems(firstNonNull(primary.getMinItems(), secondary.getMinItems()))
        .uniqueItems(firstNonNull(primary.getUniqueItems(), secondary.getUniqueItems()))
        .maxProperties(firstNonNull(primary.getMaxProperties(), secondary.getMaxProperties()))
        .minProperties(firstNonNull(primary.getMinProperties(), secondary.getMinProperties()))
        .required(merge(primary.getRequired(), secondary.getRequired()))
        .not(merge(primary.getNot(), secondary.getNot()))
        .properties(merge(primary.getProperties(), secondary.getProperties(), OpenAPIMergerUtil::merge, Schema.class))
        .additionalProperties(firstNonNull(primary.getAdditionalProperties(), secondary.getAdditionalProperties()))
        .description(firstNotBlank(primary.getDescription(), secondary.getDescription()))
        .$ref(firstNotBlank(primary.get$ref(), secondary.get$ref()))
        .nullable(null)
        .readOnly(firstNonNull(primary.getReadOnly(), secondary.getReadOnly()))
        .writeOnly(firstNonNull(primary.getWriteOnly(), secondary.getWriteOnly()))
        .externalDocs(merge(primary.getExternalDocs(), secondary.getExternalDocs()))
        .deprecated(firstNonNull(primary.getDeprecated(), secondary.getDeprecated()))
        .xml(firstNonNull(primary.getXml(), secondary.getXml()))
        .extensions(merge(primary.getExtensions(), secondary.getExtensions()))
        .discriminator(firstNonNull(primary.getDiscriminator(), secondary.getDiscriminator()))
        .prefixItems(merge(primary.getPrefixItems(), secondary.getPrefixItems()))
        .allOf(merge(primary.getAllOf(), secondary.getAllOf()))
        .anyOf(merge(primary.getAnyOf(), secondary.getAnyOf()))
        .oneOf(merge(primary.getOneOf(), secondary.getOneOf()))
        .items(merge(primary.getItems(), secondary.getItems()))
        .types(types)
        .patternProperties(merge(primary.getPatternProperties(), secondary.getPatternProperties(), OpenAPIMergerUtil::merge, Schema.class))
        .exclusiveMaximumValue(firstNonNull(primary.getExclusiveMaximumValue(), secondary.getExclusiveMaximumValue()))
        .exclusiveMinimumValue(firstNonNull(primary.getExclusiveMinimumValue(), secondary.getExclusiveMinimumValue()))
        .contains(merge(primary.getContains(), secondary.getContains()))
        .$id(firstNotBlank(primary.get$id(), secondary.get$id()))
        .$schema(firstNotBlank(primary.get$schema(), secondary.get$schema()))
        .$anchor(firstNotBlank(primary.get$anchor(), secondary.get$anchor()))
        .$vocabulary(firstNotBlank(primary.get$vocabulary(), secondary.get$vocabulary()))
        .$dynamicAnchor(firstNotBlank(primary.get$dynamicAnchor(), secondary.get$dynamicAnchor()))
        .$dynamicRef(firstNotBlank(primary.get$dynamicRef(), secondary.get$dynamicRef()))
        .contentEncoding(firstNotBlank(primary.getContentEncoding(), secondary.getContentEncoding()))
        .contentMediaType(firstNotBlank(primary.getContentMediaType(), secondary.getContentMediaType()))
        .contentSchema(merge(primary.getContentSchema(), secondary.getContentSchema()))
        .propertyNames(merge(primary.getPropertyNames(), secondary.getPropertyNames()))
        .unevaluatedProperties(merge(primary.getUnevaluatedProperties(), secondary.getUnevaluatedProperties()))
        .maxContains(firstNonNull(primary.getMaxContains(), secondary.getMaxContains()))
        .minContains(firstNonNull(primary.getMinContains(), secondary.getMinContains()))
        .additionalItems(merge(primary.getAdditionalItems(), secondary.getAdditionalItems()))
        .unevaluatedItems(merge(primary.getUnevaluatedItems(), secondary.getUnevaluatedItems()))
        ._if(merge(primary.getIf(), secondary.getIf()))
        ._else(merge(primary.getElse(), secondary.getElse()))
        .then(merge(primary.getThen(), secondary.getThen()))
        .dependentSchemas(merge(primary.getDependentSchemas(), secondary.getDependentSchemas(), OpenAPIMergerUtil::merge, Schema.class))
        .dependentRequired(merge(primary.getDependentRequired(), secondary.getDependentRequired()))
        .$comment(firstNotBlank(primary.get$comment(), secondary.get$comment()))
        .booleanSchemaValue(firstNonNull(primary.getBooleanSchemaValue(), secondary.getBooleanSchemaValue()))
        .examples(firstNonNull(primary.getExamples(), secondary.getExamples()))
        .example(firstNonNull(primary.getExample(), secondary.getExample()))
        ._enum(firstNonNull(primary.getEnum(), secondary.getEnum()))
        ._const(firstNonNull(primary.getConst(), secondary.getConst()))
      ;
    }
  }

  private static Tag merge(final Tag primary, final Tag secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Tag merged = new Tag();
      merged.name(firstNotBlank(primary.getName(), secondary.getName()));
      merged.description(firstNotBlank(primary.getDescription(), secondary.getDescription()));
      merged.externalDocs(merge(primary.getExternalDocs(), secondary.getExternalDocs()));
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      return merged;
    }
  }

  private static ApiResponse merge(final ApiResponse primary, final ApiResponse secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final ApiResponse merged = new ApiResponse();
      merged.description(firstNotBlank(primary.getDescription(), secondary.getDescription()));
      merged.setHeaders(merge(primary.getHeaders(), secondary.getHeaders()));
      merged.setContent(merge(primary.getContent(), secondary.getContent()));
      merged.setLinks(merge(primary.getLinks(), secondary.getLinks()));
      merged.setExtensions(merge(primary.getExtensions(), secondary.getExtensions()));
      merged.set$ref(firstNotBlank(primary.get$ref(), secondary.get$ref()));
      return merged;
    }
  }

  private static Content merge(final Content primary, final Content secondary) {
    if (secondary == null) {
      return primary;
    } else if (primary == null) {
      return secondary;
    } else {
      final Content merged = new Content();
      merged.putAll(primary);
      secondary.forEach(merged::putIfAbsent);
      return merged;
    }
  }
}
