package io.avaje.htmx.nima.jstache;

import io.avaje.htmx.nima.TemplateContentCache;
import io.helidon.webserver.http.ServerRequest;

import java.util.concurrent.ConcurrentHashMap;

public class SimpleContentCache implements TemplateContentCache {

  private final ConcurrentHashMap<String,String> localCache = new ConcurrentHashMap<>();

  @Override
  public String key(ServerRequest req) {
    return req.requestedUri().path().rawPath();
  }

  @Override
  public String key(ServerRequest req, Object formParams) {
    return req.requestedUri().path().rawPath() + formParams;
  }

  @Override
  public String content(String key) {
    return localCache.get(key);
  }

  @Override
  public void contentPut(String key, String content) {
    localCache.put(key, content);
  }
}
