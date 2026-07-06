package org.example.myapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.myapp.web.NullMarkedClassDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NullMarkedControllerTest {
  @Test
  public void testClass() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream openapiFile = NullMarkedClassDTO.class.getResourceAsStream("/public/openapi.json")) {
      JsonNode root = mapper.readTree(openapiFile);

      JsonNode schemas = root.path("components").path("schemas");

      JsonNode nullMarkedClassDTO = schemas.get("NullMarkedClassDTO");
      assertEquals("[\"map1\",\"map2\",\"map3\",\"set1\",\"set2\",\"string1\",\"stringArray1\",\"stringArray2\"]", nullMarkedClassDTO.get("required").toString());

      JsonNode properties = nullMarkedClassDTO.get("properties");
      assertType(properties.get("string1"), "string");
      assertType(properties.get("string2"), "string", "null");

      assertType(properties.get("stringArray1"), "array");
      assertType(properties.get("stringArray1").get("items"), "string");
      assertType(properties.get("stringArray2"), "array");
      assertType(properties.get("stringArray2").get("items"), "string", "null");

      assertType(properties.get("set1"), "array");
      assertType(properties.get("set1").get("items"), "string");
      assertType(properties.get("set2"), "array");
      assertType(properties.get("set2").get("items"), "string", "null");
      assertType(properties.get("set3"), "array", "null");
      assertType(properties.get("set3").get("items"), "string");

      assertType(properties.get("map1"), "object");
      assertType(properties.get("map1").get("additionalProperties"), "string");
      assertType(properties.get("map2"), "object");
      assertType(properties.get("map2").get("additionalProperties"), "string");
      assertType(properties.get("map3"), "object");
      assertType(properties.get("map3").get("additionalProperties"), "string", "null");
      assertType(properties.get("map4"), "object", "null");
      assertType(properties.get("map4").get("additionalProperties"), "string");
    }
  }

  @Test
  public void testRecord() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream openapiFile = NullMarkedClassDTO.class.getResourceAsStream("/public/openapi.json")) {
      JsonNode root = mapper.readTree(openapiFile);

      JsonNode schemas = root.path("components").path("schemas");

      JsonNode nullMarkedRecordDTO = schemas.get("NullMarkedRecordDTO");
      assertEquals("[\"notNullable\"]", nullMarkedRecordDTO.get("required").toString());
      assertType(nullMarkedRecordDTO.get("properties").get("notNullable"), "string");
      assertType(nullMarkedRecordDTO.get("properties").get("nullable"), "string", "null");
    }
  }

  private static void assertType(JsonNode schema, String... expectedTypes) {
    JsonNode type = schema.get("type");
    if (expectedTypes.length == 1) {
      assertEquals(expectedTypes[0], type.asText());
      return;
    }
    List<String> actualTypes = new ArrayList<>();
    type.forEach(typeNode -> actualTypes.add(typeNode.asText()));
    assertEquals(Arrays.asList(expectedTypes), actualTypes);
  }
}
