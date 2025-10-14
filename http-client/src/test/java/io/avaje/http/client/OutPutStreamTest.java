package io.avaje.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

public class OutPutStreamTest {

  private static HttpServer server;
  private static int port;
  private static final AtomicReference<String> receivedBody = new AtomicReference<>();

  @BeforeAll
  static void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    port = server.getAddress().getPort();
    server.createContext(
        "/test",
        exchange -> {
          try (var is = exchange.getRequestBody();
              ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            is.transferTo(baos);

            receivedBody.set(baos.toString());
          }
          exchange.sendResponseHeaders(204, -1);
        });
    server.setExecutor(Executors.newSingleThreadExecutor());
    server.start();
  }

  @AfterAll
  static void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void testOutputStreamBodyPublisher() throws Exception {
    String data = "Hello OutputStreamBodyPublisher!";

    HttpClient client = HttpClient.builder().requestTimeout(Duration.ofDays(1)).build();

    HttpResponse<String> response =
        client
            .request()
            .url("http://localhost:" + port + "/test")
            .header("Content-Type", "text/plain")
            .body(
                outputStream -> {
                  outputStream.write(data.getBytes());
                })
            .POST()
            .asPlainString();

    assertEquals(204, response.statusCode());
    assertEquals(data, receivedBody.get());
  }

  @Test
  void testOutputStreamBodyPublisherLargeData() throws Exception {
    int repeat = 100_000; // much larger than buffer (8192)
    String chunk = "abcdefghij"; // 10 bytes
    StringBuilder sb = new StringBuilder(repeat * chunk.length());
    for (int i = 0; i < repeat; i++) {
      sb.append(chunk);
    }
    String data = sb.toString();

    receivedBody.set(null); // reset

    HttpClient client = HttpClient.builder().requestTimeout(Duration.ofMinutes(2)).build();

    HttpResponse<String> response =
        client
            .request()
            .url("http://localhost:" + port + "/test")
            .header("Content-Type", "text/plain")
            .body(
                os -> {
                  for (int i = 0; i < repeat; i++) {
                    os.write(chunk.getBytes());
                  }
                })
            .POST()
            .asPlainString();

    assertEquals(204, response.statusCode());
    assertEquals(data, receivedBody.get());
  }

  @Test
  void testError() throws Exception {

    HttpClient client = HttpClient.builder().requestTimeout(Duration.ofDays(1)).build();
    try {

      client
          .request()
          .url("http://localhost:" + port + "/test")
          .header("Content-Type", "text/plain")
          .body(
              outputStream -> {
                outputStream.write(" Output".getBytes());
                throw new IOException("test error");
              })
          .POST()
          .asPlainString();
    } catch (HttpException e) {
      assertEquals("test error", e.getCause().getMessage());
    }
  }
}
