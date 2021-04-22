package io.avaje.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Logs request and response details for debug logging purposes.
 */
public class RequestLogger implements RequestListener {

  private static final Logger log = LoggerFactory.getLogger(RequestLogger.class);

  private final String delimiter;

  public RequestLogger() {
    this("\n");
  }

  public RequestLogger(String delimiter) {
    this.delimiter = delimiter;
  }

  @Override
  public void response(Event event) {
    if (log.isDebugEnabled()) {
      final HttpResponse<?> response = event.response();
      final HttpRequest request = response.request();
      long micros = event.responseTimeNanos() / 1000;

      StringBuilder sb = new StringBuilder();
      sb.append("statusCode:").append(response.statusCode())
        .append(" method:").append(request.method())
        .append(" uri:").append(event.uri())
        .append(" timeMicros:").append(micros);

      headers(sb, "req-head: ", request.headers());
      body(sb, "req-body: ", event.requestBody());
      headers(sb, "res-head: ", response.headers());
      body(sb, "res-body: ", event.responseBody());
      log.debug(sb.toString());
    }
  }

  private void body(StringBuilder sb, String label, String body) {
    if (body != null) {
      sb.append(delimiter).append(label).append(body);
    }
  }

  private void headers(StringBuilder sb, String label, HttpHeaders headers) {
    final Set<Map.Entry<String, List<String>>> entries = headers.map().entrySet();
    if (!entries.isEmpty()) {
      sb.append(delimiter).append(label);
      for (Map.Entry<String, List<String>> entry : entries) {
        sb.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
      }
    }
  }
}
