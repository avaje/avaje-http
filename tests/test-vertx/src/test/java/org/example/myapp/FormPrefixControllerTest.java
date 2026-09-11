package org.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class FormPrefixControllerTest extends BaseWebTest {

  @Test
  void nestedFormPrefix() throws Exception {
    String body = "name=bill"
      + "&invoice.street=" + enc("Main Street")
      + "&invoice.city=" + enc("Springfield")
      + "&invoice.zip=12345"
      + "&invoice.contact.phone=555-1234"
      + "&invoice.contact.email=" + enc("bob@example.com")
      + "&shipping.street=" + enc("Square One")
      + "&shipping.city=" + enc("Shelbyville");

    HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/formprefix"))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.body()).isEqualTo(
      "bill|Main Street,Springfield,12345|555-1234@bob@example.com|Square One,Shelbyville");
  }

  @Test
  void nestedFormPrefix_partial() throws Exception {
    HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/formprefix"))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString("name=ann"))
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.body()).isEqualTo("ann|null,null,null|null@null|null,null");
  }

  private static String enc(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
