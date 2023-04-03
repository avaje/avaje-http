package io.avaje.http.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Mark a field on a BeanParam/FormParam class as not a request parameter of any kind */
@Target(FIELD)
@Retention(SOURCE)
public @interface IgnoreBeanField {}
