package io.avaje.http.generator.client.clients;

import io.avaje.http.client.HttpException;

public class MappedException extends RuntimeException {

  private final int statusCode;
  private String responseMessage;
  private MyJsonErrorPayload myPayload;

  public MappedException(HttpException httpException) {
    super("Error response with statusCode: " + httpException.statusCode());
    this.statusCode = httpException.statusCode();
    if (httpException.isPlainText()) {
      this.responseMessage = httpException.bodyAsString();
    } else {
      this.myPayload = httpException.bean(MyJsonErrorPayload.class);
    }
    addSuppressed(httpException);
  }

  public int statusCode() {
    return statusCode;
  }

  public String responseMessage() {
    return responseMessage;
  }

  public MyJsonErrorPayload myPayload() {
    return myPayload;
  }

  // @Json
  static class MyJsonErrorPayload {
    // fields
  }
}
