package io.avaje.htmx.nima;

import io.helidon.http.Header;
import io.helidon.http.ServerRequestHeaders;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import static io.avaje.htmx.nima.HxHeaders.*;

final class DHxHandler implements Handler {


  private final Handler delegate;
  private final String target;
  private final String trigger;
  private final String triggerName;

  DHxHandler(Handler delegate, String target, String trigger, String triggerName) {
    this.delegate = delegate;
    this.target = target;
    this.trigger = trigger;
    this.triggerName = triggerName;
  }

  @Override
  public void handle(ServerRequest req, ServerResponse res) throws Exception {
    final var headers = req.headers();
    if (headers.contains(HX_REQUEST) && matched(headers)) {
      delegate.handle(req, res);
    } else {
      res.next();
    }
  }

  private boolean matched(ServerRequestHeaders headers) {
    if (target != null && notMatched(headers.get(HX_TARGET), target)) {
      return false;
    }
    if (trigger != null && notMatched(headers.get(HX_TRIGGER), trigger)) {
      return false;
    }
    return triggerName == null || !notMatched(headers.get(HX_TRIGGER_NAME), triggerName);
  }

  private boolean notMatched(Header header, String matchValue) {
    return header == null || !matchValue.equals(header.get());
  }

}
