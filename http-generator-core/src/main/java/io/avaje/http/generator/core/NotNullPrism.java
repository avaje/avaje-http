package io.avaje.http.generator.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.processing.Generated;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/** A Prism representing a {@link org.jetbrains.annotations.NotNull @NotNull} annotation. */
@Generated("avaje-prism-generator")
public final class NotNullPrism {
  /** store prism value of value */
  private final String _value;

  public static final String PRISM_TYPE = "org.jetbrains.annotations.NotNull";

  /**
   * An instance of the Values inner class whose methods return the AnnotationValues used to build
   * this prism. Primarily intended to support using Messager.
   */
  public final Values values;

  /**
   * Returns true if the mirror is an instance of {@link org.jetbrains.annotations.NotNull @NotNull}
   * is present on the element, else false.
   *
   * @param mirror mirror.
   * @return true if prism is present.
   */
  public static boolean isInstance(AnnotationMirror mirror) {
    return getInstance(mirror) != null;
  }

  /**
   * Returns true if {@link org.jetbrains.annotations.NotNull @NotNull} is present on the element,
   * else false.
   *
   * @param element element.
   * @return true if annotation is present on the element.
   */
  public static boolean isPresent(Element element) {
    return getInstanceOn(element) != null;
  }

  /**
   * Return a prism representing the {@link org.jetbrains.annotations.NotNull @NotNull} annotation
   * present on the given element. similar to {@code element.getAnnotation(NotNull.class)} except
   * that an instance of this class rather than an instance of {@link
   * org.jetbrains.annotations.NotNull @NotNull} is returned.
   *
   * @param element element.
   * @return prism on element or null if no annotation is found.
   */
  public static NotNullPrism getInstanceOn(Element element) {
    final var mirror = getMirror(element);
    if (mirror == null) return null;
    return getInstance(mirror);
  }

  /**
   * Return a Optional representing a nullable {@link org.jetbrains.annotations.NotNull @NotNull}
   * annotation on the given element. similar to {@link
   * element.getAnnotation(org.jetbrains.annotations.NotNull.class)} except that an Optional of this
   * class rather than an instance of {@link org.jetbrains.annotations.NotNull} is returned.
   *
   * @param element element.
   * @return prism optional for element.
   */
  public static Optional<NotNullPrism> getOptionalOn(Element element) {
    final var mirror = getMirror(element);
    if (mirror == null) return Optional.empty();
    return getOptional(mirror);
  }

  /**
   * Return a prism of the {@link org.jetbrains.annotations.NotNull @NotNull} annotation from an
   * annotation mirror.
   *
   * @param mirror mirror.
   * @return prism for mirror or null if mirror is an incorrect type.
   */
  public static NotNullPrism getInstance(AnnotationMirror mirror) {
    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return null;

    return new NotNullPrism(mirror);
  }

  /**
   * Return an Optional representing a nullable {@link NotNullPrism @NotNullPrism} from an
   * annotation mirror. similar to {@link e.getAnnotation(org.jetbrains.annotations.NotNull.class)}
   * except that an Optional of this class rather than an instance of {@link
   * org.jetbrains.annotations.NotNull @NotNull} is returned.
   *
   * @param mirror mirror.
   * @return prism optional for mirror.
   */
  public static Optional<NotNullPrism> getOptional(AnnotationMirror mirror) {
    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString()))
      return Optional.empty();

    return Optional.of(new NotNullPrism(mirror));
  }

  private NotNullPrism(AnnotationMirror mirror) {
    for (final ExecutableElement key : mirror.getElementValues().keySet()) {
      memberValues.put(key.getSimpleName().toString(), mirror.getElementValues().get(key));
    }
    for (final ExecutableElement member :
        ElementFilter.methodsIn(mirror.getAnnotationType().asElement().getEnclosedElements())) {
      defaults.put(member.getSimpleName().toString(), member.getDefaultValue());
    }
    _value = getValue("value", String.class);
    this.values = new Values(memberValues);
    this.mirror = mirror;
    this.isValid = valid;
  }

  /**
   * Returns a String representing the value of the {@code java.lang.String public abstract
   * java.lang.String value() } member of the Annotation.
   *
   * @see org.jetbrains.annotations.NotNull#value()
   */
  public String value() {
    return _value;
  }

  /**
   * Determine whether the underlying AnnotationMirror has no errors. True if the underlying
   * AnnotationMirror has no errors. When true is returned, none of the methods will return null.
   * When false is returned, a least one member will either return null, or another prism that is
   * not valid.
   */
  public final boolean isValid;

  /**
   * The underlying AnnotationMirror of the annotation represented by this Prism. Primarily intended
   * to support using Messager.
   */
  public final AnnotationMirror mirror;
  /**
   * A class whose members corespond to those of {@link org.jetbrains.annotations.NotNull @NotNull}
   * but which each return the AnnotationValue corresponding to that member in the model of the
   * annotations. Returns null for defaulted members. Used for Messager, so default values are not
   * useful.
   */
  public static final class Values {
    private final Map<String, AnnotationValue> values;

    private Values(Map<String, AnnotationValue> values) {
      this.values = values;
    }
    /**
     * Return the AnnotationValue corresponding to the value() member of the annotation, or null
     * when the default value is implied.
     */
    public AnnotationValue value() {
      return values.get("value");
    }
  }

  private final Map<String, AnnotationValue> defaults = new HashMap<>(10);
  private final Map<String, AnnotationValue> memberValues =
      new HashMap<>(10);
  private boolean valid = true;

  private <T> T getValue(String name, Class<T> clazz) {
    final T result = NotNullPrism.getValue(memberValues, defaults, name, clazz);
    if (result == null) valid = false;
    return result;
  }

  private static AnnotationMirror getMirror(Element target) {
    for (final var m : target.getAnnotationMirrors()) {
      final CharSequence mfqn =
          ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName();
      if (PRISM_TYPE.contentEquals(mfqn)) return m;
    }
    return null;
  }

  private static <T> T getValue(
      Map<String, AnnotationValue> memberValues,
      Map<String, AnnotationValue> defaults,
      String name,
      Class<T> clazz) {
    AnnotationValue av = memberValues.get(name);
    if (av == null) av = defaults.get(name);
    if (av == null) {
      return null;
    }
    if (clazz.isInstance(av.getValue())) return clazz.cast(av.getValue());
    return null;
  }
}
