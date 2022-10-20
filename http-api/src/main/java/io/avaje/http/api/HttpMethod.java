package io.avaje.http.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Base for Http verb based annotations. */
@Target(value = ElementType.ANNOTATION_TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface HttpMethod {

  String value();
}
