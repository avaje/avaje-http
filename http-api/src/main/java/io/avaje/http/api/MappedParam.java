package io.avaje.http.api;

import static java.lang.annotation.ElementType.MODULE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a type to be mapped. */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface MappedParam {

  /** Factory method name used to construct the type. Empty means use a constructor */
  String factoryMethod() default "";

  @Repeatable(MappedParam.Import.Imports.class)
  @Retention(SOURCE)
  @Target({TYPE, PACKAGE, MODULE})
  @interface Import {

    Class<?> value();

    /** Factory method name used to construct the type. Empty means use a constructor */
    String factoryMethod() default "";

    @Retention(SOURCE)
    @Target({TYPE, PACKAGE, MODULE})
    @interface Imports {

      Import[] value();
    }
  }
}
