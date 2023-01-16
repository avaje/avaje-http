package io.avaje.http.client;

import java.time.Instant;

/**
 * Represents an Authorization Bearer token that can be held on the context.
 * <p>
 * Typically the token will be valid for a period and then expire.
 */
public interface AuthToken {

  /**
   * Return the Authorization bearer token.
   */
  String token();

  /**
   * Return true if the token has expired or is no longer valid.
   */
  boolean isExpired();

  /**
   * Create an return a AuthToken with the given token and time it is valid until.
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
  }
}
