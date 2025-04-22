package io.avaje.http.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class BGInvoke {

  static void invoke(Runnable task) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      executor.submit(task);
    } finally {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }
}
