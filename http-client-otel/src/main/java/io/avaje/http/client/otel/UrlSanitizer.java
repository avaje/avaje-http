package io.avaje.http.client.otel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

final class UrlSanitizer {

  private UrlSanitizer() {
  }

  static String sanitize(String url, Set<String> sensitiveQueryParameters) {
    try {
      final URI uri = URI.create(url);
      if (uri.getScheme() == null || uri.getHost() == null) {
        return url;
      }
      final String query = sanitizeQuery(uri.getRawQuery(), sensitiveQueryParameters);
      final String userInfo = uri.getRawUserInfo() == null ? null : "REDACTED:REDACTED";
      return new URI(
        uri.getScheme(),
        userInfo,
        uri.getHost(),
        uri.getPort(),
        uri.getRawPath(),
        query,
        uri.getRawFragment())
        .toString();
    } catch (IllegalArgumentException | URISyntaxException e) {
      return url;
    }
  }

  private static String sanitizeQuery(String rawQuery, Set<String> sensitiveQueryParameters) {
    if (rawQuery == null || rawQuery.isEmpty()) {
      return rawQuery;
    }

    final String[] parts = rawQuery.split("&", -1);
    for (int i = 0; i < parts.length; i++) {
      parts[i] = sanitizeQueryPart(parts[i], sensitiveQueryParameters);
    }
    return String.join("&", parts);
  }

  private static String sanitizeQueryPart(String rawPart, Set<String> sensitiveQueryParameters) {
    final int equalsPos = rawPart.indexOf('=');
    final String key = equalsPos == -1 ? rawPart : rawPart.substring(0, equalsPos);
    if (!sensitiveQueryParameters.contains(key) || equalsPos == -1) {
      return rawPart;
    }
    return key + "=REDACTED";
  }
}
