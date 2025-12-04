package io.avaje.http.client;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class JDK21Functions {
  private JDK21Functions() {}

  static ExecutorService getExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  static void closeClient(HttpClient client) {
    client.close();
  }
}
