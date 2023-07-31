package io.avaje.http.api;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Base for Http verb based annotations. */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
public @interface HttpMethod {

  String value();
}
