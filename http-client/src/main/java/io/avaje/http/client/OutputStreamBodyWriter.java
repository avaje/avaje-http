package io.avaje.http.client;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Use to set the body content using a callback that writes to an {@link java.io.OutputStream}.
 * <p>
 * This allows streaming large or dynamically generated content directly to the HTTP request body,
 * without buffering the entire payload in memory. The provided {@code OutputStreamWriter} is called
 * with an {@link java.io.OutputStream} that writes to the request body. Data written to the stream
 * is sent as the request body.
 * <p>
 * Example usage:
 * <pre>{@code
 *   client.request()
 *     .url("http://example.com/upload")
 *     .body(outputStream -> {
 *       // Write data in chunks
 *       for (byte[] chunk : getChunks()) {
 *         outputStream.write(chunk);
 *       }
 *     })
 *     .POST()
 *     .asPlainString();
 * }</pre>
 *
 * @see HttpClientRequest#body(OutputStreamBodyWriter)
 */
public interface OutputStreamBodyWriter {

  /**
   * Write body content to the outputStream.
   */
  void write(OutputStream outputStream) throws IOException;
}
