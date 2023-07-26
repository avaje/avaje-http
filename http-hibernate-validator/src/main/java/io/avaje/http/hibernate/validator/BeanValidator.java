package io.avaje.http.hibernate.validator;

import java.util.*;

import io.avaje.http.api.ValidationException;
import io.avaje.http.api.ValidationException.ViolationMessage;
import io.avaje.http.api.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
    List<ViolationMessage> errors = new ArrayList<>();

    for (final ConstraintViolation<?> violation : violations) {
      final var path = violation.getPropertyPath().toString();
      final var field = pathToField(path);
      final var message = violation.getMessage();
      errors.add(new ViolationMessage(path, field, message));
    }

    var cause = new ConstraintViolationException(violations);
    throw new ValidationException(422, "Request failed validation", cause, errors);
  }

  private String pathToField(String path) {
    int pos = path.lastIndexOf('.');
    if (pos == -1) {
      return path;
    } else {
      return path.substring(pos + 1);
    }
  }
}
