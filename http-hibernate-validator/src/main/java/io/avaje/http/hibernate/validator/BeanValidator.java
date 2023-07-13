package io.avaje.http.hibernate.validator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.avaje.http.api.ValidationException;
import io.avaje.http.api.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

public class BeanValidator implements Validator {

  private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

  @Override
  public void validate(Object bean, String acceptLanguage, Class<?>... groups) throws ValidationException {
    final Set<ConstraintViolation<Object>> violations = factory.getValidator().validate(bean, groups);
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
