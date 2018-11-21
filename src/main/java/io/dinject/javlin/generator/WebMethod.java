package io.dinject.javlin.generator;

enum WebMethod {
  GET(200),
  PUT(200),
  POST(201),
  PATCH(200),
  DELETE(200);

  private int statusCode;

  WebMethod(int statusCode) {
    this.statusCode = statusCode;
  }

  int statusCode() {
    return statusCode;
  }
}
