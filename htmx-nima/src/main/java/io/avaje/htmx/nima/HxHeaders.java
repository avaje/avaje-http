package io.avaje.htmx.nima;

import io.helidon.http.HeaderName;
import io.helidon.http.HeaderNames;

/**
 * HTMX request headers.
 *
 * @see <a href="https://htmx.org/reference/#request_headers">Request Headers Reference</a>
 */
public interface HxHeaders {

  /**
   * Indicates that the request comes from an element that uses hx-boost.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Boosted</a>
   */
  HeaderName HX_BOOSTED = HeaderNames.create("HX-Boosted");

  /**
   * The current URL of the browser
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Current-URL</a>
   */
  HeaderName HX_CURRENT_URL = HeaderNames.create("HX-Current-URL");

  /**
   * Indicates if the request is for history restoration after a miss in the local history cache.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-History-Restore-Request</a>
   */
  HeaderName HX_HISTORY_RESTORE_REQUEST = HeaderNames.create("HX-History-Restore-Request");

  /**
   * Contains the user response to a <a href="https://htmx.org/attributes/hx-prompt/">hx-prompt</a>.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Prompt</a>
   */
  HeaderName HX_PROMPT = HeaderNames.create("HX-Prompt");
  /**
   * Only present and {@code true} if the request is issued by htmx.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Request</a>
   */
  HeaderName HX_REQUEST = HeaderNames.create("HX-Request");
  /**
   * The {@code id} of the target element if it exists.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Target</a>
   */
  HeaderName HX_TARGET = HeaderNames.create("HX-Target");
  /**
   * The {@code name} of the triggered element if it exists
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Trigger-Name</a>
   */
  HeaderName HX_TRIGGER_NAME = HeaderNames.create("HX-Trigger-Name");
  /**
   * The {@code id} of the triggered element if it exists.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Trigger</a>
   */
  HeaderName HX_TRIGGER = HeaderNames.create("HX-Trigger");

}
