package io.avaje.http.client;

import java.util.concurrent.ExecutorService;

final class VirtualThreadExecutor {
  private VirtualThreadExecutor() {}

  static ExecutorService getVTExecutor() {
    return null;
  }
}
