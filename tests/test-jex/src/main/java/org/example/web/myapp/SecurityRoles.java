package org.example.web.myapp;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.example.web.AppRoles;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Specify permitted roles.
 */
@SecurityRequirement(name = "JWT")
@Target(value={METHOD, TYPE})
@Retention(value=RUNTIME)
public @interface SecurityRoles {

  /**
   * Specify the permitted roles.
   */
  AppRoles[] value() default {};
}
