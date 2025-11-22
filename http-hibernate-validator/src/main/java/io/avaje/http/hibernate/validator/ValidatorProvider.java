package io.avaje.http.hibernate.validator;

import io.avaje.http.api.Validator;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.PluginProvides;
import jakarta.validation.Validation;

/** Plugin for avaje inject that provides a default BeanValidator instance. */
@PluginProvides(Validator.class)
public final class ValidatorProvider implements io.avaje.inject.spi.InjectPlugin {

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(
        Validator.class,
        () -> {
          var validator = new BeanValidator();
          builder.addPostConstruct(
              b ->
                  validator.postConstruct(
                      b.getOptional(jakarta.validation.Validator.class)
                          .orElseGet(
                              () -> Validation.buildDefaultValidatorFactory().getValidator())));
          return validator;
        });
  }
}
