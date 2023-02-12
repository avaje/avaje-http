package io.avaje.http.generator.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * A Prism representing an {@code @javax.inject.Inject} annotation. <br>
 * Added manually because we can't have both javax and jakarta as dependecies
 */
public class JavaxInjectPrism {
  public static final String PRISM_TYPE = "javax.inject.Inject";

  /**
   * An instance of the Values inner class whose methods return the AnnotationValues used to build
   * this prism. Primarily intended to support using Messager.
   */
  final Values values;
  /**
   * Return a prism representing the {@code @javax.inject.Inject} annotation on 'e'. similar to
   * {@code e.getAnnotation(javax.inject.Inject.class)} except that an instance of this class rather
   * than an instance of {@code javax.inject.Inject} is returned.
   */
  static JavaxInjectPrism getInstanceOn(Element e) {
    final var m = getMirror(PRISM_TYPE, e);
    if (m == null) return null;
    return getInstance(m);
  }

  /** Return a prism of the {@code @javax.inject.Inject} annotation whose mirror is mirror. */
  static JavaxInjectPrism getInstance(AnnotationMirror mirror) {
    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return null;

    return new JavaxInjectPrism(mirror);
  }

  private JavaxInjectPrism(AnnotationMirror mirror) {
    for (final ExecutableElement key : mirror.getElementValues().keySet()) {
      memberValues.put(key.getSimpleName().toString(), mirror.getElementValues().get(key));
    }
    for (final ExecutableElement member :
        ElementFilter.methodsIn(mirror.getAnnotationType().asElement().getEnclosedElements())) {
      defaults.put(member.getSimpleName().toString(), member.getDefaultValue());
    }
    this.values = new Values(memberValues);
    this.mirror = mirror;
    this.isValid = valid;
  }

  /**
   * Determine whether the underlying AnnotationMirror has no errors. True if the underlying
   * AnnotationMirror has no errors. When true is returned, none of the methods will return null.
   * When false is returned, a least one member will either return null, or another prism that is
   * not valid.
   */
  final boolean isValid;

  /**
   * The underlying AnnotationMirror of the annotation represented by this Prism. Primarily intended
   * to support using Messager.
   */
  final AnnotationMirror mirror;
  /**
   * A class whose members corespond to those of javax.inject.Inject but which each return the
   * AnnotationValue corresponding to that member in the model of the annotations. Returns null for
   * defaulted members. Used for Messager, so default values are not useful.
   */
  static class Values {
    private final Map<String, AnnotationValue> values;

    private Values(Map<String, AnnotationValue> values) {
      this.values = values;
    }
  }

  private final Map<String, AnnotationValue> defaults = new HashMap<>(10);
  private final Map<String, AnnotationValue> memberValues = new HashMap<>(10);
  private boolean valid = true;

  private <T> T getValue(String name, Class<T> clazz) {
    final var result = JavaxInjectPrism.getValue(memberValues, defaults, name, clazz);
    if (result == null) valid = false;
    return result;
  }

  private <T> List<T> getArrayValues(String name, final Class<T> clazz) {
    final List<T> result = JavaxInjectPrism.getArrayValues(memberValues, defaults, name, clazz);
    if (result == null) valid = false;
    return result;
  }

  private static AnnotationMirror getMirror(String fqn, Element target) {
    for (final AnnotationMirror m : target.getAnnotationMirrors()) {
      final CharSequence mfqn =
          ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName();
      if (fqn.contentEquals(mfqn)) return m;
    }
    return null;
  }

  private static <T> T getValue(
      Map<String, AnnotationValue> memberValues,
      Map<String, AnnotationValue> defaults,
      String name,
      Class<T> clazz) {
    var av = memberValues.get(name);
    if (av == null) av = defaults.get(name);
    if (av == null) {
      return null;
    }
    if (clazz.isInstance(av.getValue())) return clazz.cast(av.getValue());
    return null;
  }

  private static <T> List<T> getArrayValues(
      Map<String, AnnotationValue> memberValues,
      Map<String, AnnotationValue> defaults,
      String name,
      final Class<T> clazz) {
    var av = memberValues.get(name);
    if (av == null) av = defaults.get(name);
    if (av == null) {
      return java.util.Collections.EMPTY_LIST;
    }
    if (av.getValue() instanceof List) {
      final List<T> result = new ArrayList<>();
      for (final AnnotationValue v : getValueAsList(av)) {
        if (clazz.isInstance(v.getValue())) {
          result.add(clazz.cast(v.getValue()));
        } else {
          return java.util.Collections.EMPTY_LIST;
        }
      }
      return result;
    } else {
      return java.util.Collections.EMPTY_LIST;
    }
  }

  @SuppressWarnings("unchecked")
  private static List<AnnotationValue> getValueAsList(AnnotationValue av) {
    return (List<AnnotationValue>) av.getValue();
  }
}
