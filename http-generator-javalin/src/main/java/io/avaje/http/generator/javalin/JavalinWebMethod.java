package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.WebMethod;

public enum JavalinWebMethod implements WebMethod {
  BEFORE(0),
  BEFORE_MATCHED(0),
  AFTER(0),
  AFTER_MATCHED(0);

  private final int statusCode;

  JavalinWebMethod(int statusCode) {
    this.statusCode = statusCode;
  }

  @Override
  public int statusCode(boolean isVoid) {
    return statusCode;
  }
}
