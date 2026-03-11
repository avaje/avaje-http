package io.avaje.http.maven.openapi.jsonb;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

class SchemaCustomAdaptorTest {

  @Test
  void parseAndWriteTypeArray() {
    final Jsonb jsonb =
        Jsonb.builder().serializeEmpty(true).serializeNulls(false).failOnUnknown(false).build();
    final JsonType<Schema> schemaType = jsonb.type(Schema.class);

    final Schema schema = schemaType.fromJson("{\"type\":[\"string\",\"null\"]}");

    assertThat(schema.getType()).isEqualTo("string");
    assertThat(schema.getTypes()).isNotNull();
    assertThat(schema.getTypes()).contains("string", "null");

    final String json = schemaType.toJson(schema);
    assertThat(json).contains("\"type\"");
    assertThat(json).doesNotContain("\"types\"");
  }

  @Test
  void parseLegacyTypesAliasAndWriteAsType() {
    final Jsonb jsonb =
        Jsonb.builder().serializeEmpty(true).serializeNulls(false).failOnUnknown(false).build();
    final JsonType<Schema> schemaType = jsonb.type(Schema.class);

    final Schema schema = schemaType.fromJson("{\"types\":[\"integer\",\"null\"]}");

    assertThat(schema.getType()).isEqualTo("integer");
    assertThat(schema.getTypes()).isNotNull();
    assertThat(schema.getTypes()).contains("integer", "null");

    final String json = schemaType.toJson(schema);
    assertThat(json).contains("\"type\"");
    assertThat(json).doesNotContain("\"types\"");
  }

  @Test
  void parseNullableTrueWritesTypeUnion() {
    final Jsonb jsonb =
        Jsonb.builder().serializeEmpty(true).serializeNulls(false).failOnUnknown(false).build();
    final JsonType<Schema> schemaType = jsonb.type(Schema.class);

    final Schema schema = schemaType.fromJson("{\"type\":\"string\",\"nullable\":true}");

    assertThat(schema.getNullable()).isNull();
    assertThat(schema.getTypes()).containsExactly("string", "null");

    final String json = schemaType.toJson(schema);
    assertThat(json).contains("\"type\":[\"string\",\"null\"]");
    assertThat(json).doesNotContain("\"nullable\"");
  }

  @Test
  void parseNullableFalseRemovesNullFromTypeUnion() {
    final Jsonb jsonb =
        Jsonb.builder().serializeEmpty(true).serializeNulls(false).failOnUnknown(false).build();
    final JsonType<Schema> schemaType = jsonb.type(Schema.class);

    final Schema schema =
        schemaType.fromJson("{\"type\":[\"string\",\"null\"],\"nullable\":false}");

    assertThat(schema.getNullable()).isNull();
    assertThat(schema.getTypes()).containsExactly("string");

    final String json = schemaType.toJson(schema);
    assertThat(json).contains("\"type\":\"string\"");
    assertThat(json).doesNotContain("\"nullable\"");
  }
}
