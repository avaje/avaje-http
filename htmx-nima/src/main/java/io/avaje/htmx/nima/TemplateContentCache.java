package io.avaje.htmx.nima;

import io.helidon.webserver.http.ServerRequest;

/**
 * Defines caching of template content.
 */
public interface TemplateContentCache {

  /**
   * Return the key given the request.
   */
  String key(ServerRequest req);

  /**
   * Return the key given the request with form parameters.
   */
  String key(ServerRequest req, Object formParams);

  /**
   * Return the content given the key.
   */
  String content(String key);

  /**
   * Put the content into the cache.
   */
  void contentPut(String key, String content);

}
