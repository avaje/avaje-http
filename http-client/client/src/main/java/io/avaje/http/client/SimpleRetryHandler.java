package io.avaje.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;

/**
 * Simple retry with max attempts and linear backoff.
 */
public class SimpleRetryHandler implements RetryHandler {

  private static final Logger log = LoggerFactory.getLogger(SimpleRetryHandler.class);

  private final int maxRetries;
  private final long backoffMillis;

  public SimpleRetryHandler(int maxRetries, long backoffMillis) {
    this.maxRetries = maxRetries;
    this.backoffMillis = backoffMillis;
  }

  @Override
  public boolean isRetry(int retryCount, HttpResponse<?> response) {
    if (response.statusCode() < 500 || retryCount >= maxRetries) {
      return false;
    }
    log.debug("retry count:{} status:{} uri:{}", retryCount, response.statusCode(), response.uri());
    try {
      Thread.sleep(backoffMillis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
    return true;
  }
}
