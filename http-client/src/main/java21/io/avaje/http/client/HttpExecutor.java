package io.avaje.http.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class HttpExecutor {
  private HttpExecutor() {}
  static ExecutorService getExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
