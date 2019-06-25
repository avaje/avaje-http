package io.dinject.javalin.generator;

public enum WebMethod {
  GET(200),
  POST(201),
  PUT(200, 204),
  PATCH(200, 204),
  DELETE(200, 204);

  private int statusCode;
  private int voidStatusCode;

  WebMethod(int statusCode, int voidStatusCode) {
    this.statusCode = statusCode;
    this.voidStatusCode = voidStatusCode;
  }

  WebMethod(int statusCode) {
    this.statusCode = statusCode;
    this.voidStatusCode = statusCode;
  }

  int statusCode(boolean isVoid) {
    return isVoid ? voidStatusCode : statusCode;
  }
}
