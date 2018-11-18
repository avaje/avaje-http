package io.kanuka.web.javlin;

public enum WebMethod {
  GET(200),
  PUT(200),
  POST(201),
  PATCH(200),
  DELETE(200);

  int statusCode;

  WebMethod(int statusCode) {
    this.statusCode = statusCode;
  }

  int statusCode() {
    return statusCode;
  }
}
