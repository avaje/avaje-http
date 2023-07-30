package io.avaje.http.generator.helidon.nima;

import io.avaje.http.generator.core.WebMethod;

public enum HelidonWebMethod implements WebMethod {
  FILTER(0);

  private int statusCode;

  HelidonWebMethod(int statusCode) {
    this.statusCode = statusCode;
  }

  @Override
  public int statusCode(boolean isVoid) {
    return statusCode;
  }
}
