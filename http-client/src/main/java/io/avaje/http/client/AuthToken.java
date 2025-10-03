package io.avaje.http.client;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents an Authorization Bearer token that can be held on the context.
 */
public interface AuthToken {

  /**
   * The Authorization bearer token.
   */
  String token();

  /**
   * Whether the token has expired or no longer valid.
   */
  boolean isExpired();

  /**
   * Duration until expiration.
   */
  Duration expiration();

  /**
   * Create an AuthToken with the given token and when it expires.
   */
  static AuthToken of(String token, Instant validUntil) {
    return new Basic(token, validUntil);
  }

  /**
   * Standard AuthToken implementation.
   */
  class Basic implements AuthToken {

    private final String token;
    private final Instant validUntil;

    /**
     * Create with token and valid until time.
     */
    public Basic(String token, Instant validUntil) {
      this.token = token;
      this.validUntil = validUntil;
    }

    @Override
    public String token() {
      return token;
    }

    @Override
    public boolean isExpired() {
      return Instant.now().isAfter(validUntil);
    }

    @Override
    public Duration expiration() {
      return Duration.between(Instant.now(), validUntil);
    }
  }
}
