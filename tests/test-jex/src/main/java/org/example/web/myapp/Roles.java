package org.example.web.myapp;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.example.web.AppRoles;

/**
 * Specify permitted roles.
 */
@Target(value={METHOD, TYPE})
@Retention(value=RUNTIME)
public @interface Roles {

  /**
   * Specify the permitted roles.
   */
  AppRoles[] value() default {};
}
