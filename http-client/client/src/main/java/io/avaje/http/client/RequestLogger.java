package io.avaje.http.client;

import io.avaje.applog.AppLog;

import java.lang.System.Logger.Level;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Logs request and response details for debug logging purposes using <code>System.Logger</code>.
 * <p>
 * This implementation logs the request and response with the same single logging entry
 * rather than separate logging of the request and response.
 * <p>
 * With logging level set to {@code DEBUG} for {@code io.avaje.http.client.RequestLogger} the
 * request and response are logged as a summary with response status and time.
 * <p>
 * Set the logging level to {@code TRACE} to include the request and response headers and body
 * payloads with truncation for large bodies.
 * <p>
 * Using System.Logger, messages by default go to JUL (Java Util Logging) unless a provider
 * is registered. We can use <em>io.avaje:avaje-slf4j-jpl</em> to have System.Logger
 * messages go to <em>slf4j-api</em>.
 */
public class RequestLogger implements RequestListener {

  private static final System.Logger log = AppLog.getLogger("io.avaje.http.client.RequestLogger");

  private final String delimiter;

  /**
   * Create using the {@literal \n} new line character.
   */
  public RequestLogger() {
    this("\n");
  }

  /**
   * Create with a given line delimiter.
   */
  public RequestLogger(String delimiter) {
    this.delimiter = delimiter;
  }

  @Override
  public void response(Event event) {
    if (log.isLoggable(Level.DEBUG)) {
      final HttpResponse<?> response = event.response();
      final HttpRequest request = response.request();
      long micros = event.responseTimeMicros();

      StringBuilder sb = new StringBuilder();
      sb.append("statusCode:").append(response.statusCode())
        .append(" method:").append(request.method())
        .append(" uri:").append(event.uri())
        .append(" timeMicros:").append(micros);

      if (log.isLoggable(Level.TRACE)) {
        headers(sb, "req-head: ", request.headers());
        body(sb, "req-body: ", event.requestBody());
        headers(sb, "res-head: ", response.headers());
        body(sb, "res-body: ", event.responseBody());
      }
      log.log(Level.DEBUG, sb.toString());
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
        final String key = entry.getKey();
        if (obfuscate(key)) {
          sb.append(key).append("=<obfuscated>, ");
        } else {
          sb.append(key).append("=").append(entry.getValue()).append(", ");
        }
      }
    }
  }

  boolean obfuscate(String key) {
    return DHttpClientContext.AUTHORIZATION.equals(key);
  }
}
