import io.avaje.http.hibernate.validator.ValidatorProvider;

module io.avaje.http.hibernate.validator {

  requires transitive io.avaje.http.api;
  requires transitive io.avaje.inject;
  requires transitive jakarta.validation;

  provides io.avaje.inject.spi.InjectExtension with ValidatorProvider;
}
