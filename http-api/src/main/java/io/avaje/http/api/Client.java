package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker annotation for client.
 *
 * <pre>{@code
 * @Client
 * interface CustomerApi {
 *   ...
 *   @Get("/{id}")
 *   Customer getById(long id);
 *
 *   @Post
 *   long save(Customer customer);
 * }
 *
 * }</pre>
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Client {

  /** Specify the path mapping request to the controller. */
  String value() default "";

  /**
   * Flag to set whether to generate a Client Implementation. Set false if the interface exists merely to be extended by
   * other client interfaces
   */
  boolean generate() default true;

  /**
   * Specify <code>@Client.Import</code> on a package or class to refer to the client interface we
   * want to generate.
   *
   * <pre>{@code
   * @Client.Import(types = OtherApi.class)
   * package org.example;
   *
   * }</pre>
   */
  @Target({TYPE, PACKAGE})
  @Retention(RUNTIME)
  @interface Import {

    /**
     * Client interface types that we want to generate HTTP clients for.
     */
    Class<?>[] value();
  }
}
