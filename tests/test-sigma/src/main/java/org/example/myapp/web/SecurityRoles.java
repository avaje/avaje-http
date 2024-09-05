package org.example.myapp.web;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
