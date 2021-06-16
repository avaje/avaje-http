package io.avaje.http.client;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Listen to responses.
 * <p>
 * {@link RequestLogger} is an implementation for debug logging
 * the requests and responses.
 */
public interface RequestListener {

  /**
   * Handle the response.
   */
  void response(Event event);

  /**
   * The response event details.
   */
  interface Event {

    /**
     * Return the time from request to response in nanos.
     */
    long responseTimeNanos();

    /**
     * Return the URI for the request.
     */
    URI uri();

    /**
     * Return the response.
     */
    HttpResponse<?> response();

    /**
     * Return the related request.
     */
    HttpRequest request();

    /**
     * Return the response body as string content if applicable.
     * <p>
     * This is primarily here to assist logging of responses for trace purposes.
     * <p>
     * This will return null if the response is not String or byte array
     * encoded string content. For example, when requests use response
     * handlers for InputStream, Path, Stream etc this will return null.
     */
    String responseBody();

    /**
     * Return the related request body as string content if available.
     * <p>
     * This is primarily here to assist logging of requests for trace purposes.
     * <p>
     * This will return  null if the related request content was not
     * String or byte array encoded string content.
     */
    String requestBody();

  }
}
