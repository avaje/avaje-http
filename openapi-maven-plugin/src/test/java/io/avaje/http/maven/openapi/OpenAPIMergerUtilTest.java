package io.avaje.http.maven.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpenAPIMergerUtilTest {

  @Test
  void mergeSchemaConvertsNullableToTypeUnion() {
    final Schema<?> primarySchema = new StringSchema().nullable(true);
    final Schema<?> secondarySchema = new StringSchema();

    final OpenAPI primary =
        new OpenAPI()
            .openapi("3.1.2")
            .components(new Components().addSchemas("Thing", primarySchema));
    final OpenAPI secondary =
        new OpenAPI()
            .openapi("3.1.2")
            .components(new Components().addSchemas("Thing", secondarySchema));

    final OpenAPI merged = OpenAPIMergerUtil.merge(primary, secondary);
    final Schema<?> schema = merged.getComponents().getSchemas().get("Thing");

    assertThat(schema.getNullable()).isNull();
    assertThat(schema.getTypes()).containsExactly("string", "null");
  }

  @Test
  void mergeSchemaKeepsPrimaryNullabilityWhenSecondaryNullableFalse() {
    final Schema<?> primarySchema =
        new StringSchema().types(new LinkedHashSet<>(List.of("string", "null")));
    final Schema<?> secondarySchema = new StringSchema().nullable(false);

    final OpenAPI primary =
        new OpenAPI()
            .openapi("3.1.2")
            .components(new Components().addSchemas("Thing", primarySchema));
    final OpenAPI secondary =
        new OpenAPI()
            .openapi("3.1.2")
            .components(new Components().addSchemas("Thing", secondarySchema));

    final OpenAPI merged = OpenAPIMergerUtil.merge(primary, secondary);
    final Schema<?> schema = merged.getComponents().getSchemas().get("Thing");

    assertThat(schema.getNullable()).isNull();
    assertThat(schema.getTypes()).containsExactly("string", "null");
  }

  @Test
  void mergeSchemaUsesSecondaryNullabilityWhenPrimaryHasNoTypeInformation() {
    final Schema<?> primarySchema = new Schema<>();
    final Schema<?> secondarySchema = new StringSchema().nullable(false);

    final OpenAPI primary =
        new OpenAPI()
            .openapi("3.1.2")
            .components(new Components().addSchemas("Thing", primarySchema));
    final OpenAPI secondary =
        new OpenAPI()
            .openapi("3.1.2")
            .components(new Components().addSchemas("Thing", secondarySchema));

    final OpenAPI merged = OpenAPIMergerUtil.merge(primary, secondary);
    final Schema<?> schema = merged.getComponents().getSchemas().get("Thing");

    assertThat(schema.getNullable()).isNull();
    assertThat(schema.getTypes()).containsExactly("string");
  }

  @Test
  void mergeOpenApiVersionPrefersHighestMinor() {
    final var primary =
        new OpenAPI().openapi("3.1.2");
    final var secondary =
        new OpenAPI().openapi("3.2.0");

    final OpenAPI merged = OpenAPIMergerUtil.merge(primary, secondary);

    assertThat(merged.getOpenapi()).isEqualTo("3.2.0");
  }

  @Test
  void mergeSchemaConvertsExclusiveBoundsToNumericBounds() {
    final Schema<?> primarySchema =
        new NumberSchema().minimum(new BigDecimal("1.5")).exclusiveMinimum(true);
    final Schema<?> secondarySchema =
        new NumberSchema().maximum(new BigDecimal("9.5")).exclusiveMaximum(true);

    final OpenAPI primary =
        new OpenAPI()
            .openapi("3.1.2")
            .components(new Components().addSchemas("Thing", primarySchema));
    final OpenAPI secondary =
        new OpenAPI()
            .openapi("3.1.2")
            .components(new Components().addSchemas("Thing", secondarySchema));

    final OpenAPI merged = OpenAPIMergerUtil.merge(primary, secondary);
    final Schema<?> schema = merged.getComponents().getSchemas().get("Thing");

    assertThat(schema.getMinimum()).isNull();
    assertThat(schema.getMaximum()).isNull();
    assertThat(schema.getExclusiveMinimum()).isNull();
    assertThat(schema.getExclusiveMaximum()).isNull();
    assertThat(schema.getExclusiveMinimumValue()).isEqualByComparingTo("1.5");
    assertThat(schema.getExclusiveMaximumValue()).isEqualByComparingTo("9.5");
  }
}
