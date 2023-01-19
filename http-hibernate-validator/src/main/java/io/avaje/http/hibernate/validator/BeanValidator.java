package io.avaje.http.hibernate.validator;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import io.avaje.http.api.ValidationException;
import io.avaje.http.api.Validator;

public class BeanValidator implements Validator {

  private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

  @Override
  public void validate(Object bean) {
    final Set<ConstraintViolation<Object>> violations = factory.getValidator().validate(bean);
    if (!violations.isEmpty()) {
      throwExceptionWith(violations);
    }
  }

  private void throwExceptionWith(Set<ConstraintViolation<Object>> violations) {
    final Map<String, Object> errors = new LinkedHashMap<>();
    for (final ConstraintViolation<?> violation : violations) {
      final var path = violation.getPropertyPath();
      final var message = violation.getMessage();
      errors.put(path.toString(), message);
    }

    throw new ValidationException(422, "Request failed validation", errors);
  }
}
