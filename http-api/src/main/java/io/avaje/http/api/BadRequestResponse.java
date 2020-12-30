package io.avaje.http.api;

/*
 Extendable response class, if this class is extended it will be automatically added to the openapi json file.
 The default status code is 400 but can be changed with for example @StatusCode("404")
 */
public class BadRequestResponse extends Exception {
  public final String message;

  protected BadRequestResponse(String message) {
    this.message = message;
  }
}
