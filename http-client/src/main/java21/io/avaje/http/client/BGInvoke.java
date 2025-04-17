package io.avaje.http.client;

import java.util.concurrent.Executors;

final class BGInvoke {

  static void invoke(Runnable task) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      executor.submit(task);
    }
  }
}
