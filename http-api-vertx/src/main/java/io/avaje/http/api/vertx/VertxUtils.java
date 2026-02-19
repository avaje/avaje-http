package io.avaje.http.api.vertx;

import io.vertx.ext.web.RoutingContext;

public class VertxUtils {

  private VertxUtils() {}

  public static String cookieValue(RoutingContext ctx, String cookieName) {
    final var cookie = ctx.request().getCookie(cookieName);
    return cookie != null ? cookie.getValue() : null;
  }
}
