package io.avaje.http.client;

import java.util.List;

final class DRequestListeners implements RequestListener {

  private final RequestListener[] listeners;

  DRequestListeners(List<RequestListener> reqListeners) {
    this.listeners = reqListeners.toArray(new RequestListener[0]);
  }

  @Override
  public void response(Event event) {
    for (RequestListener listener : listeners) {
      listener.response(event);
    }
  }
}
