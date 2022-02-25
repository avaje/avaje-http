package io.avaje.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Random;

/**
 * Simple retry with max attempts and linear backoff.
 */
public class SimpleRetryHandler implements RetryHandler {

  private static final Logger log = LoggerFactory.getLogger(SimpleRetryHandler.class);

  private final int maxRetries;
  private final long backoffMillis;
  private final int gitterMillis;
  private final Random random;

  /**
   * Create with maximum number of retries and linear backoff time.
   *
   * @param maxRetries    The maximum number of retry attempts
   * @param backoffMillis The linear backoff between attempts in milliseconds
   * @param gitterMillis  The maximum amount of gitter that gets added to backoffMillis
   */
  public SimpleRetryHandler(int maxRetries, int backoffMillis, int gitterMillis) {
    this.maxRetries = maxRetries;
    this.backoffMillis = backoffMillis;
    this.gitterMillis = gitterMillis;
    this.random = new Random();
  }

  /**
   * Create with maximum number of retries and linear backoff time and no gitter.
   *
   * @param maxRetries    The maximum number of retry attempts
   * @param backoffMillis The linear backoff between attempts in milliseconds
   */
  public SimpleRetryHandler(int maxRetries, int backoffMillis) {
    this(maxRetries, backoffMillis, 0);
  }

  @Override
  public boolean isRetry(int retryCount, HttpResponse<?> response) {
    if (response.statusCode() < 500 || retryCount >= maxRetries) {
      return false;
    }
    log.debug("retry count:{} status:{} uri:{}", retryCount, response.statusCode(), response.uri());
    try {
      int gitter = gitterMillis < 1 ? 0 : random.nextInt(gitterMillis);
      Thread.sleep(backoffMillis + gitter);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
    return true;
  }
}
