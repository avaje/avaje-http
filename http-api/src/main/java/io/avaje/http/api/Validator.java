package io.avaje.http.api;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

/** Validator for form beans or request beans. */
public interface Validator {

  /**
   * Validate the bean throwing an exception if the bean fails validation.
   *
   * <p>Typically the exception will be handled by a specific exception handler returning a 422 or
   * 400 status code and usually a map of field paths to error messages.
   *
   * @param bean The bean to validate
   */
  void validate(Object bean, String acceptLanguage, Class<?>... groups) throws ValidationException;

  default Locale resolveLocale(String acceptLanguage, Collection<Locale> locales) {
    if (acceptLanguage == null) {
      return null;
    }
    final List<LanguageRange> list = LanguageRange.parse(acceptLanguage);
    return Locale.lookup(list, locales);
  }
}
