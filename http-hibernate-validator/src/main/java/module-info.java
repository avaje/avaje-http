import io.avaje.http.hibernate.validator.ValidatorProvider;

module io.avaje.http.hibernate.validator {

  exports io.avaje.http.hibernate.validator;

  requires io.avaje.http.api;
  requires io.avaje.inject;
  requires jakarta.validation;

  provides io.avaje.inject.spi.InjectExtension with ValidatorProvider;
}
