package io.avaje.http.client;

import java.io.IOException;
import java.io.OutputStream;

public interface OutputStreamBodyWriter {
  void write(OutputStream outputStream) throws IOException;
}
