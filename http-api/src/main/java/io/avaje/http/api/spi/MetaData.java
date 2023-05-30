package io.avaje.http.api.spi;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.*;

/**
 * For internal use, holds metadata on generated client interfaces for use by code generation (Java
 * annotation processing).
 */
@Target(TYPE)
@Retention(CLASS)
public @interface MetaData {

  /** The generated HttpClient interfaces. */
  Class<?>[] value();
}
