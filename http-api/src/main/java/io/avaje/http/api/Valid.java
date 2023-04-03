package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Add {@code @Valid} annotation on a controller/method/BeanParam that we want bean validation to
 * be included for. When we do this controller methods that take a request payload will then have
 * the request bean (populated by JSON payload or form parameters) validated before it is passed
 * to the controller method.
 * <p>
 * When trying to validate a {@code @BeanParam} bean, this will need to be placed on the BeanParam type.
 * <p>
 * When using this annotation we need to provide an implementation of {@link Validator} to use.
 * <p>
 * Alternatively we can use the Jakarta {@code @Valid} along with a Jakarta validator implementation.
 */
@Retention(SOURCE)
@Target({METHOD, TYPE, PARAMETER})
public @interface Valid {
}
