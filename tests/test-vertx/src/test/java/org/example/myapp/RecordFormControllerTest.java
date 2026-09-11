package org.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class RecordFormControllerTest extends BaseWebTest {

  @Test
  void recordFormPrefix() throws Exception {
    String body = "firstName=bill"
      + "&lastName=smith&lastName=jones"
      + "&invoice.street=" + enc("Main Street")
      + "&invoice.city=" + enc("Springfield");

    HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/recordform"))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.body()).isEqualTo("bill|[smith, jones]|Main Street,Springfield");
  }

  private static String enc(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
