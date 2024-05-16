package io.avaje.htmx.nima;

import io.helidon.webserver.http.Handler;

public interface HxHandler {

  static Builder builder(Handler delegate) {
    return new DHxHandlerBuilder(delegate);
  }

  interface Builder {

    Builder target(String target);

    Builder trigger(String trigger);

    Builder triggerName(String triggerName);

    Handler build();
  }
}
