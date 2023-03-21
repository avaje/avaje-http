package io.avaje.http.api;

import java.io.IOException;
import java.io.OutputStream;

import io.helidon.nima.webserver.http.ServerResponse;

public class NimaJsonbStream extends OutputStream {

  private final ServerResponse response;
  private OutputStream outputStream;
  private byte[] firstBuffer;
  private int firstOff;
  private int firstLen;
  private boolean chunked;

  public NimaJsonbStream(ServerResponse response) {
    this.response = response;
  }

  @Override
  public void write(int b) throws IOException {}

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (chunked) {
      outputStream.write(b, off, len);
    } else if (firstBuffer == null) {
      firstBuffer = b;
      firstOff = off;
      firstLen = len;
    } else {
      chunked = true;
      outputStream = response.outputStream();
      outputStream.write(firstBuffer, firstOff, firstLen);
      outputStream.write(b, off, len);
      firstBuffer = null;
    }
  }

  @Override
  public void flush() throws IOException {
    if (!chunked) {
      response.contentLength(firstLen);
      outputStream = response.outputStream();
      outputStream.write(firstBuffer, firstOff, firstLen);
    }
    outputStream.flush();
  }
}
