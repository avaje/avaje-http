package io.avaje.htmx.api;

import io.avaje.lang.Nullable;

/**
 * This class can be used as a controller method argument to access
 * the <a href="https://htmx.org/reference/#request_headers">htmx Request Headers</a>.
 *
 * <pre>{@code
 *
 *   @HxRequest
 *   @Get("/users")
 *   String users(HtmxRequest htmxRequest) {
 *     if (htmxRequest.isBoosted()) {
 *         ...
 *     }
 *   }
 *
 * }</pre>
 *
 * @see <a href="https://htmx.org/reference/#request_headers">Request Headers Reference</a>
 */
public interface HtmxRequest {

  /**
   * Represents a non-Htmx request.
   */
  HtmxRequest EMPTY = new DHxRequest();

  /**
   * Return a new builder for the HtmxRequest.
   */
  static Builder builder() {
    return new DHxRequest.DBuilder();
  }

  /**
   * Return true if this is an Htmx request.
   */
  boolean isHtmxRequest();

  /**
   * Indicates that the request is via an element using hx-boost.
   *
   * @return true if the request was made via HX-Boost, false otherwise
   */
  boolean isBoosted();

  /**
   * Return the current URL of the browser when the htmx request was made.
   */
  @Nullable
  String currentUrl();

  /**
   * Indicates if the request is for history restoration after a miss in the local history cache
   *
   * @return true if this request is for history restoration, false otherwise
   */
  boolean isHistoryRestoreRequest();

  /**
   * Return the user response to an HX-Prompt.
   */
  @Nullable
  String promptResponse();

  /**
   * Return the id of the target element if it exists.
   */
  @Nullable
  String target();

  /**
   * Return the name of the triggered element if it exists.
   */
  @Nullable
  String triggerName();

  /**
   * Return the id of the triggered element if it exists.
   */
  @Nullable
  String triggerId();

  /**
   * Builder for {@link HtmxRequest}.
   */
  interface Builder {
    Builder boosted(boolean boosted);

    Builder currentUrl(String currentUrl);

    Builder historyRestoreRequest(boolean historyRestoreRequest);

    Builder promptResponse(String promptResponse);

    Builder target(String target);

    Builder triggerName(String triggerName);

    Builder triggerId(String triggerId);

    HtmxRequest build();
  }
}
