package io.avaje.http.client;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

/**
 * Internal Http Client interface.
 */
interface SpiHttpClient {

  /**
   * Return a UrlBuilder to use to build an URL taking into
   * account the base URL.
   */
  UrlBuilder url();

  /**
   * Return the underlying http client.
   */
  HttpClient httpClient();

  /**
   * Return the response content taking into account content encoding.
   *
   * @param httpResponse The HTTP response to decode the content from
   * @return The decoded content
   */
  BodyContent readContent(HttpResponse<byte[]> httpResponse);

  /**
   * Decode the response content given the <code>Content-Encoding</code> http header.
   *
   * @param httpResponse The HTTP response
   * @return The decoded content
   */
  byte[] decodeContent(HttpResponse<byte[]> httpResponse);

  /**
   * Decode the body using the given encoding.
   *
   * @param encoding The encoding used to decode the content
   * @param content  The raw content being decoded
   * @return The decoded content
   */
  byte[] decodeContent(String encoding, byte[] content);

  /**
   * Check the response status code and throw HttpException if the status
   * code is in the error range.
   */
  void checkResponse(HttpResponse<?> response);

}
