package io.avaje.http.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class HttpExecutor {
  private HttpExecutor() {}
  //only executed in tests
  static ExecutorService getExecutor() {
    return Executors.newCachedThreadPool();
  }
}
