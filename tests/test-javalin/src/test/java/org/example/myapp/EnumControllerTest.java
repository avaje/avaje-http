package org.example.myapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.myapp.web.NullMarkedClassDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumControllerTest {

  static JsonNode root;

  @BeforeAll
  public static void init() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream openapiFile = NullMarkedClassDTO.class.getResourceAsStream("/public/openapi.json")) {
      root = mapper.readTree(openapiFile);
    }
  }

  @Test
  public void testComponents() {
      JsonNode components = root.path("components").path("schemas");
      JsonNode enumExample = components.get("EnumExample");
      assertEquals("{\"type\":\"string\",\"enum\":[\"ENUM_VALUE_1\",\"ENUM_VALUE_2\"]}", enumExample.toString());

      JsonNode enumDTO = components.get("EnumDTO");
      assertEquals("{\"type\":\"object\",\"properties\":{\"value1\":{\"$ref\":\"#/components/schemas/EnumExample\"},\"value2\":{\"$ref\":\"#/components/schemas/EnumExample\"}}}", enumDTO.toString());
  }

  @Test
  public void testFirst() {
    JsonNode endpoint = root.path("paths").path("/enum/first").path("get");

    JsonNode response = endpoint.path("responses").path("200").path("content").path("application/json").path("schema").path("$ref");
    assertEquals("\"#/components/schemas/EnumDTO\"", response.toString());
  }

  @Test
  public void testSecond() {
    JsonNode endpoint = root.path("paths").path("/enum/second").path("get");

    JsonNode response = endpoint.path("responses").path("200").path("content").path("application/json").path("schema").path("$ref");
    assertEquals("\"#/components/schemas/EnumDTO\"", response.toString());

    JsonNode parameter = endpoint.path("parameters").get(0);
    assertEquals("enumExample", parameter.path("name").asText());
    assertEquals("query", parameter.path("in").asText());
    assertEquals("#/components/schemas/EnumExample", parameter.path("schema").path("$ref").asText());
  }

  @Test
  public void testThird() {
    JsonNode endpoint = root.path("paths").path("/enum/third").path("post");

    JsonNode response = endpoint.path("responses").path("201").path("content").path("application/json").path("schema").path("$ref");
    assertEquals("\"#/components/schemas/EnumDTO\"", response.toString());

    JsonNode requestBody = endpoint.path("requestBody").path("content").path("application/json").path("schema").path("$ref");
    assertEquals("#/components/schemas/EnumExample", requestBody.asText());
  }

  @Test
  public void testFourth() {
    JsonNode endpoint = root.path("paths").path("/enum/fourth").path("post");

    JsonNode response = endpoint.path("responses").path("201").path("content").path("application/json").path("schema").path("$ref");
    assertEquals("\"#/components/schemas/EnumDTO\"", response.toString());

    JsonNode requestBody = endpoint.path("requestBody").path("content").path("application/json").path("schema").path("$ref");
    assertEquals("#/components/schemas/EnumDTO", requestBody.asText());
  }
}
