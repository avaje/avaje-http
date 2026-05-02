package org.example.myapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.myapp.web.NullMarkedClassDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

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

      assertEquals("{\"type\":\"string\",\"nullable\":false}", nullMarkedClassDTO.get("properties").get("string1").toString());
      assertEquals("{\"type\":\"string\"}", nullMarkedClassDTO.get("properties").get("string2").toString());

      assertEquals("{\"type\":\"array\",\"nullable\":false,\"items\":{\"type\":\"string\"}}", nullMarkedClassDTO.get("properties").get("stringArray1").toString());
      assertEquals("{\"type\":\"array\",\"nullable\":false,\"items\":{\"type\":\"string\"}}", nullMarkedClassDTO.get("properties").get("stringArray2").toString());

      assertEquals("{\"type\":\"array\",\"nullable\":false,\"items\":{\"type\":\"string\",\"nullable\":false}}", nullMarkedClassDTO.get("properties").get("set1").toString());
      assertEquals("{\"type\":\"array\",\"nullable\":false,\"items\":{\"type\":\"string\"}}", nullMarkedClassDTO.get("properties").get("set2").toString());
      assertEquals("{\"type\":\"array\",\"items\":{\"type\":\"string\",\"nullable\":false}}", nullMarkedClassDTO.get("properties").get("set3").toString());

      assertEquals("{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\",\"nullable\":false},\"nullable\":false}", nullMarkedClassDTO.get("properties").get("map1").toString());
      assertEquals("{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\",\"nullable\":false},\"nullable\":false}", nullMarkedClassDTO.get("properties").get("map2").toString());
      assertEquals("{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\"},\"nullable\":false}", nullMarkedClassDTO.get("properties").get("map3").toString());
      assertEquals("{\"type\":\"object\",\"additionalProperties\":{\"type\":\"string\",\"nullable\":false}}", nullMarkedClassDTO.get("properties").get("map4").toString());
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
      assertEquals("{\"type\":\"string\",\"nullable\":false}", nullMarkedRecordDTO.get("properties").get("notNullable").toString());
      assertEquals("{\"type\":\"string\"}", nullMarkedRecordDTO.get("properties").get("nullable").toString());
    }
  }
}
