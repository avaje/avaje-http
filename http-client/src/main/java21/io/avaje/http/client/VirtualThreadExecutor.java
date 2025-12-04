package io.avaje.http.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class VirtualThreadExecutor {
  private VirtualThreadExecutor() {}

  static ExecutorService getVTExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
