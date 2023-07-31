package io.avaje.http.generator.core;

public interface CustomWebMethod {

  WebMethod webMethod();

  default String value() {
    return "";
  }
}
