package io.avaje.http.api.context;

/**
 * Holder for the Server Request/Response instances.
 */
public final class ServerContext {

  private final Object request;
  private final Object response;

  public ServerContext(Object request, Object response) {
    this.request = request;
    this.response = response;
  }

  /**
   * Retrieve the current server request.
   *
   * @return The request
   */
  @SuppressWarnings("unchecked")
  public <T> T request() {
    return (T) request;
  }

  /**
   * Retrieve the current server response.
   *
   * @return The request
   */
  @SuppressWarnings("unchecked")
  public <T> T response() {
    return (T) response;
  }
}
