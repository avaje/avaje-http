package org.example.myapp;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ValidateOpenAPISpecTest {
  @Test
  public void testValidateJson() throws IOException {
    try (InputStream openapiFile = ValidateOpenAPISpecTest.class.getResourceAsStream("/public/openapi.json")) {
      assertNotNull(openapiFile);
      try (InputStreamReader isr = new InputStreamReader(openapiFile); BufferedReader reader = new BufferedReader(isr)) {
          String openAPIContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
          SwaggerParseResult result = new OpenAPIV3Parser().readContents(openAPIContent, null, null);
          if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            for (var message: result.getMessages()) {
              System.out.println(message);
            }
            fail("There are issues with the OpenAPI file");
          }
      }
    }
  }
}
