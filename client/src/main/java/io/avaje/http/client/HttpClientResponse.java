package io.avaje.http.client;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Controls how the response is processed including potential
 * conversion into beans.
 */
public interface HttpClientResponse {

  /**
   * Returning the response using the given response reader.
   *
   * @param reader The response reader.
   * @param <T>    The type that the content is converted to.
   * @return The response converted into the appropriate bean via the reader.
   */
  <T> T read(BodyReader<T> reader);

  /**
   * Return the response as a single bean.
   *
   * @param type The type of the bean to convert the response content into.
   * @param <T>  The type that the content is converted to.
   * @return The bean the response is converted into.
   */
  <T> T bean(Class<T> type);

  /**
   * Return the response as a list of beans.
   *
   * @param type The type of the bean to convert the response content into.
   * @param <T>  The type that the content is converted to.
   * @return The list of beans the response is converted into.
   */
  <T> List<T> list(Class<T> type);

  /**
   * Return the content as string.
   */
  HttpResponse<String> asString();

  /**
   * Return the response discarding the response content.
   */
  HttpResponse<Void> asVoid();

  /**
   * Return the content as InputStream.
   */
  HttpResponse<InputStream> asInputStream();

  /**
   * Return the content as a stream of string lines.
   */
  HttpResponse<Stream<String>> asLines();

  /**
   * Return the content as byte array.
   */
  HttpResponse<byte[]> asByteArray();

  /**
   * Return the content into the given file.
   */
  HttpResponse<Path> asFile(Path file);

  /**
   * Return the response using the given response body handler.
   */
  <T> HttpResponse<T> withResponseHandler(HttpResponse.BodyHandler<T> responseHandler);

}
