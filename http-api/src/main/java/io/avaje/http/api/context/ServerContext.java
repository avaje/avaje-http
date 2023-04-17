package io.avaje.http.api.context;

/** Holder for the Server Request/Response Classes */
public class ServerContext {

  private final Object request;
  private final Object response;

  public ServerContext(Object req, Object res) {
    request = req;
    response = res;
  }

  /**
   * Retrieve the current server request.
   *
   * @return The request
   */
  <T> T request() {
    return (T) request;
  }

  /**
   * Retrieve the current server response.
   *
   * @return The request
   */
  <T> T response() {
    return (T) response;
  }
}
