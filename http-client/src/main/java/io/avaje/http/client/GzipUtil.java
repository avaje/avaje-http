package io.avaje.http.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

final class GzipUtil {
  private GzipUtil() {}

  static byte[] gzip(String content) {
    return gzip(content.getBytes(StandardCharsets.UTF_8));
  }

  static byte[] gzip(byte[] content) {
    var baos = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
      gzip.write(content);
    } catch (IOException e) {
      throw new UncheckedIOException("Error while gzip encoding content", e);
    }
    return baos.toByteArray();
  }

  static byte[] gzipDecode(byte[] content) {
    try (final var unzip = new GZIPInputStream(new ByteArrayInputStream(content))) {
      return unzip.readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException("Error while gzip decoding content", e);
    }
  }
}
