package io.avaje.http.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class GzipUtil {

  static byte[] gzip(String content) {
    return gzip(content.getBytes(StandardCharsets.UTF_8));
  }

  static byte[] gzip(byte[] content) {
    try {
      ByteArrayOutputStream obj = new ByteArrayOutputStream();
      try (GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
        gzip.write(content);
      }
      return obj.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Error while gzip encoding content", e);
    }
  }

  static byte[] gzipDecode(byte[] content) {
    try {
      try (final GZIPInputStream unzip = new GZIPInputStream(new ByteArrayInputStream(content))) {
        return unzip.readAllBytes();
      }
    } catch (IOException e) {
      throw new RuntimeException("Error while gzip decoding content", e);
    }
  }
}
