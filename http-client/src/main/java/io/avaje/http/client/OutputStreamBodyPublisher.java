package io.avaje.http.client;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A BodyPublisher that allows writing to an OutputStream. Data written to the OutputStream is
 * published to the HTTP request body.
 */
final class OutputStreamBodyPublisher implements HttpRequest.BodyPublisher {

  private final PipedOutputStream outputStream;
  private final PipedInputStream inputStream;
  private final int bufferSize;
  private final AtomicBoolean streamClosed = new AtomicBoolean(false);
  private final Executor executor;
  private final OutputStreamBodyWriter writer;

  OutputStreamBodyPublisher(OutputStreamBodyWriter writer, Executor executor) {
    this.bufferSize = 8192;
    this.writer = writer;
    this.outputStream = new PipedOutputStream();
    this.inputStream = new PipedInputStream(bufferSize);
    this.executor = executor;
  }

  @Override
  public long contentLength() {
    return -1;
  }

  @Override
  public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
    try {
      outputStream.connect(inputStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    subscriber.onSubscribe(new OutputStreamSubscription(subscriber));
  }

  private class OutputStreamSubscription implements Flow.Subscription {
    private final Flow.Subscriber<? super ByteBuffer> subscriber;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private volatile boolean completed = false;
    private CompletableFuture<Void> writeTask;

    OutputStreamSubscription(Flow.Subscriber<? super ByteBuffer> subscriber) {
      this.subscriber = subscriber;

      // Start a background thread to write to the output stream
      writeTask =
          CompletableFuture.runAsync(
              () -> {
                try {
                  writer.write(outputStream);
                } catch (Throwable t) {
                  subscriber.onError(t);
                } finally {
                  try {
                    outputStream.close();
                  } catch (IOException e) {
                    subscriber.onError(e);
                  }
                }
              },
              executor);
    }

    @Override
    public void request(long n) {
      if (cancelled.get() || completed) {
        return;
      }
      try {
        byte[] buffer = new byte[bufferSize];
        for (long i = 0; i < n && !cancelled.get(); i++) {
          int bytesRead = inputStream.read(buffer);
          if (bytesRead == -1) {
            // End of stream
            completed = true;
            subscriber.onComplete();
            closeStreams();
            return;
          }
          ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
          subscriber.onNext(byteBuffer);
        }
      } catch (IOException e) {
        completed = true;
        subscriber.onError(e);
        closeStreams();
      }
    }

    @Override
    public void cancel() {
      cancelled.set(true);
      writeTask.cancel(true);
      closeStreams();
    }

    private void closeStreams() {
      if (streamClosed.compareAndSet(false, true)) {
        try (outputStream;
            inputStream; ) {
        } catch (IOException e) {
          // Ignore
        }
      }
    }
  }
}
