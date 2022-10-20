package io.avaje.http.api;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
 *
 * <h3>Client.Import</h3>
 *
 * <p>When the client interface already exists in another module we use <code>Client.Import</code>
 * to generate the client.
 *
 * <p>Specify the <code>@Client.Import</code> on the package or class to refer to the client
 * interface we want to generate.
 *
 * <pre>{@code
 * @Client.Import(types = OtherApi.class)
 * package org.example;
 *
 * }</pre>
 */
@Target(value = TYPE)
@Retention(value = RUNTIME)
public @interface Client {

  @Target(value = {TYPE, PACKAGE})
  @Retention(value = RUNTIME)
  @interface Import {

    /** Client interface types that we want to generate HTTP clients for. */
    Class<?>[] types();
  }
}
