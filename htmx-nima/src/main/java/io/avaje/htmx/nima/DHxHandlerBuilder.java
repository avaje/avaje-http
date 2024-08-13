package io.avaje.htmx.nima;

import io.helidon.webserver.http.Handler;

final class DHxHandlerBuilder implements HxHandler.Builder {

  private final Handler delegate;
  private String target;
  private String trigger;
  private String triggerName;

  DHxHandlerBuilder(Handler delegate) {
    this.delegate = delegate;
  }

  @Override
  public DHxHandlerBuilder target(String target) {
    this.target = target;
    return this;
  }

  @Override
  public DHxHandlerBuilder trigger(String trigger) {
    this.trigger = trigger;
    return this;
  }

  @Override
  public DHxHandlerBuilder triggerName(String triggerName) {
    this.triggerName = triggerName;
    return this;
  }

  @Override
  public Handler build() {
    return new DHxHandler(delegate, target, trigger, triggerName);
  }
}
