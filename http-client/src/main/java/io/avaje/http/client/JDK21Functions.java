package io.avaje.http.client;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class JDK21Functions {
  private JDK21Functions() {}

  // only executed in tests
  static ExecutorService getExecutor() {
    return Executors.newCachedThreadPool();
  }

  static void closeClient(HttpClient client) {}
}
