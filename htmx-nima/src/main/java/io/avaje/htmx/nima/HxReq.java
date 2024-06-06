package io.avaje.htmx.nima;

import io.avaje.htmx.api.HtmxRequest;
import io.helidon.webserver.http.ServerRequest;

/**
 * Obtain the HtmxRequest for the given Helidon ServerRequest.
 */
public class HxReq {

  /**
   * Create given the server request.
   */
  public static HtmxRequest of(ServerRequest request) {
    final var headers = request.headers();
    if (!headers.contains(HxHeaders.HX_REQUEST)) {
      return HtmxRequest.EMPTY;
    }

    var builder = HtmxRequest.builder();
    if (headers.contains(HxHeaders.HX_BOOSTED)) {
      builder.boosted(true);
    }
    if (headers.contains(HxHeaders.HX_HISTORY_RESTORE_REQUEST)) {
      builder.historyRestoreRequest(true);
    }
    var currentUrl = headers.get(HxHeaders.HX_CURRENT_URL);
    if (currentUrl != null) {
      builder.currentUrl(currentUrl.get());
    }
    var prompt = headers.get(HxHeaders.HX_PROMPT);
    if (prompt != null) {
      builder.promptResponse(prompt.get());
    }
    var target = headers.get(HxHeaders.HX_TARGET);
    if (target != null) {
      builder.target(target.get());
    }
    var triggerName = headers.get(HxHeaders.HX_TRIGGER_NAME);
    if (triggerName != null) {
      builder.triggerName(triggerName.get());
    }
    var trigger = headers.get(HxHeaders.HX_TRIGGER);
    if (trigger != null) {
      builder.triggerId(trigger.get());
    }
    return builder.build();
  }
}
