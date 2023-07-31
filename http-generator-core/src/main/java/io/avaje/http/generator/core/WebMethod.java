package io.avaje.http.generator.core;

public interface WebMethod {

  int statusCode(boolean isVoid);

  String name();
}
