package io.avaje.http.generator.core;

public enum CoreWebMethod implements WebMethod {
  GET(200),
  POST(201),
  PUT(200, 204),
  PATCH(200, 204),
  DELETE(200, 204),
  OTHER(0, 0);

  private int statusCode;
  private int voidStatusCode;

  CoreWebMethod(int statusCode, int voidStatusCode) {
    this.statusCode = statusCode;
    this.voidStatusCode = voidStatusCode;
  }

  CoreWebMethod(int statusCode) {
    this.statusCode = statusCode;
    this.voidStatusCode = statusCode;
  }

  @Override
  public int statusCode(boolean isVoid) {
    return isVoid ? voidStatusCode : statusCode;
  }
}
