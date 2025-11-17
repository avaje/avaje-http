package io.avaje.http.generator.core;

import java.util.Set;

class RequestScopeTypes {

  private static final String JAVALIN_CONTEXT = "io.javalin.http.Context";
  private static final String JEX_CONTEXT = "io.avaje.jex.http.Context";
  private static final String HELIDON0_REQ = "io.helidon.webserver.ServerRequest";
  private static final String HELIDON0_RES = "io.helidon.webserver.ServerResponse";
  private static final String HELIDON1_REQ = "io.helidon.webserver.http.ServerRequest";
  private static final String HELIDON1_RES = "io.helidon.webserver.http.ServerResponse";

  private static final Set<String> TYPES =
      Set.of(JAVALIN_CONTEXT, JEX_CONTEXT, HELIDON0_REQ, HELIDON0_RES, HELIDON1_REQ, HELIDON1_RES);

  /** Return true if the type is a request scoped type. */
  static boolean isRequestType(String rawType) {
    return TYPES.contains(rawType);
  }
}
