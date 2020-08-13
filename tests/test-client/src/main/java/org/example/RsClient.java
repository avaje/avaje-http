package org.example;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class RsClient {

  private final HttpClient httpClient;

  private long requestTimeout = 15;

  private final String baseUrl;

  public RsClient(HttpClient httpClient, String baseUrl) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
  }


  public void get(String uri, Type type) throws IOException, InterruptedException {

    final HttpRequest request = HttpRequest.newBuilder()
      .timeout(Duration.ofSeconds(requestTimeout))
      .uri(URI.create(baseUrl + "/" + uri))
      .GET()
      .build();

    final HttpResponse<String> res = httpClient.send(request, ofString());
  }

  public void async(String uri, Consumer<String> target, Consumer<HttpResponse<?>> errRes) {

    final HttpRequest request = HttpRequest.newBuilder()
      .timeout(Duration.ofSeconds(requestTimeout))
      .uri(URI.create(baseUrl + "/" + uri))
      .GET()
      .build();

    httpClient.sendAsync(request, ofString())
    .thenAccept(res -> {
      final int code = res.statusCode();
      if (code < 300) {
        target.accept(res.body());
      } else {
        errRes.accept(res);
        //throw new IOException("sf");
        //log.info("Failed to send metrics - response code:{} body:{}", code, res.body());
      }
    });
  }
}
