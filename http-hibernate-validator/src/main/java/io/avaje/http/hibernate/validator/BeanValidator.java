package io.avaje.http.hibernate.validator;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.avaje.http.api.ValidationException;
import io.avaje.http.api.Validator;
import io.avaje.inject.Component;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

@Component
public class BeanValidator implements Validator {

  private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

  @Override
  public void validate(Object bean) {
    Set<ConstraintViolation<Object>> violations = factory.getValidator().validate(bean);
    if (!violations.isEmpty()) {
      throwExceptionWith(violations);
    }
  }

  private void throwExceptionWith(Set<ConstraintViolation<Object>> violations) {
    Map<String, Object> errors = new LinkedHashMap<>();
    for (ConstraintViolation<?> violation : violations) {
      Path path = violation.getPropertyPath();
      String message = violation.getMessage();
      errors.put(path.toString(), message);
    }

    throw new ValidationException(422, "Request failed validation", errors);
  }
}
