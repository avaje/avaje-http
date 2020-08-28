package io.avaje.http.generator.core;

import java.util.HashSet;
import java.util.Set;

class RequestScopeTypes {

  private static final String JAVALIN_CONTEXT = "io.javalin.http.Context";
  private static final String HELIDON_REQ = "io.helidon.webserver.ServerRequest";
  private static final String HELIDON_RES = "io.helidon.webserver.ServerResponse";
  private static final Set<String> TYPES = new HashSet<>();

  static {
    TYPES.add(JAVALIN_CONTEXT);
    TYPES.add(HELIDON_REQ);
    TYPES.add(HELIDON_RES);
  }

  /**
   * Return true if the type is a request scoped type.
   */
  static boolean isRequestType(String rawType) {
    return TYPES.contains(rawType);
  }
}
