package org.example.myapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;
import org.example.myapp.web.NullMarkedClassDTO;
import org.example.myapp.web.ShortDto;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShortControllerTest extends BaseWebTest {

  final HttpClient client;

  ShortControllerTest() {
    this.client = HttpClient.builder()
      .baseUrl(baseUrl)
      .bodyAdapter(new JacksonBodyAdapter())
      .build();
  }

  @Test
  void sendAndReceiveShort() {
    ShortDto result = given()
      .param("shortValue", 12)
      .param("shortObjectValue", 125)
      .get(baseUrl + "/shorttest")
      .as(ShortDto.class);

    assertEquals((short) 12, result.shortValue);
    assertEquals((short) 125, result.shortObjectValue);
  }

  @Test
  public void testOpenAPIObject() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream openapiFile = NullMarkedClassDTO.class.getResourceAsStream("/public/openapi.json")) {
      JsonNode root = mapper.readTree(openapiFile);
      JsonNode properties = root.path("components").path("schemas").get("ShortDto").get("properties");
      JsonNode shortValue = properties.get("shortValue");
      JsonNode shortObjectValue = properties.get("shortObjectValue");

      assertEquals("integer", shortValue.get("type").asText());
      assertEquals("int32", shortValue.get("format").asText());
      assertEquals("integer", shortObjectValue.get("type").asText());
      assertEquals("int32", shortObjectValue.get("format").asText());
    }
  }

  @Test
  public void testOpenAPIPath() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream openapiFile = NullMarkedClassDTO.class.getResourceAsStream("/public/openapi.json")) {
      JsonNode root = mapper.readTree(openapiFile);
      JsonNode parameters = root.path("paths").path("/shorttest").get("get").get("parameters");
      JsonNode shortValue = parameters.get(0);
      JsonNode shortObjectValue = parameters.get(1);

      assertEquals("integer", shortValue.get("schema").get("type").asText());
      assertEquals("int32", shortValue.get("schema").get("format").asText());
      assertEquals("integer", shortObjectValue.get("schema").get("type").asText());
      assertEquals("int32", shortObjectValue.get("schema").get("format").asText());
    }
  }
}
