package io.avaje.http.hibernate.validator;

import io.avaje.http.api.Validator;
import io.avaje.inject.BeanScopeBuilder;

/**
 * Plugin for avaje inject that provides a default BeanValidator instance.
 */
public final class ValidatorProvider implements io.avaje.inject.spi.Plugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[]{Validator.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(Validator.class, BeanValidator::new);
  }
}