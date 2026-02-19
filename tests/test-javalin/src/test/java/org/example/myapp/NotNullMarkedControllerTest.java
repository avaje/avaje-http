package org.example.myapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.myapp.web.NullMarkedClassDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NotNullMarkedControllerTest {
  @Test
  public void testClass() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream openapiFile = NullMarkedClassDTO.class.getResourceAsStream("/public/openapi.json")) {
      JsonNode root = mapper.readTree(openapiFile);

      JsonNode schemas = root.path("components").path("schemas");

      JsonNode notNullMarkedClassDTO = schemas.get("NotNullMarkedClassDTO");
      assertNull(notNullMarkedClassDTO.get("required"));

      assertEquals("{\"type\":\"string\"}", notNullMarkedClassDTO.get("properties").get("string1").toString());
      assertEquals("{\"type\":\"string\"}", notNullMarkedClassDTO.get("properties").get("string2").toString());

      assertEquals("{\"type\":\"array\",\"items\":{\"type\":\"string\"}}", notNullMarkedClassDTO.get("properties").get("stringArray1").toString());
      assertEquals("{\"type\":\"array\",\"items\":{\"type\":\"string\"}}", notNullMarkedClassDTO.get("properties").get("stringArray2").toString());

      assertEquals("{\"type\":\"array\",\"items\":{\"type\":\"string\"}}", notNullMarkedClassDTO.get("properties").get("set1").toString());
      assertEquals("{\"type\":\"array\",\"items\":{\"type\":\"string\"}}", notNullMarkedClassDTO.get("properties").get("set2").toString());
      assertEquals("{\"type\":\"array\",\"items\":{\"type\":\"string\"}}", notNullMarkedClassDTO.get("properties").get("set3").toString());

      assertEquals("{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\"}}", notNullMarkedClassDTO.get("properties").get("map1").toString());
      assertEquals("{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\"}}", notNullMarkedClassDTO.get("properties").get("map2").toString());
      assertEquals("{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\"}}", notNullMarkedClassDTO.get("properties").get("map3").toString());
      assertEquals("{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\"}}", notNullMarkedClassDTO.get("properties").get("map4").toString());
    }
  }

  @Test
  public void testRecord() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream openapiFile = NullMarkedClassDTO.class.getResourceAsStream("/public/openapi.json")) {
      JsonNode root = mapper.readTree(openapiFile);

      JsonNode schemas = root.path("components").path("schemas");

      JsonNode notNullMarkedRecordDTO = schemas.get("NotNullMarkedRecordDTO");
      assertNull(notNullMarkedRecordDTO.get("required"));
      assertEquals("{\"type\":\"string\"}", notNullMarkedRecordDTO.get("properties").get("notNullable").toString());
      assertEquals("{\"type\":\"string\"}", notNullMarkedRecordDTO.get("properties").get("nullable").toString());
    }
  }
}
