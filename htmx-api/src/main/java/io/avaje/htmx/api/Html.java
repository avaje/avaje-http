package io.avaje.htmx.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mark a controller as producing HTML by default and using "Templating"
 * meaning that response objects are expected to a "Model View" passed to
 * the "Templating" library.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Html {

}
