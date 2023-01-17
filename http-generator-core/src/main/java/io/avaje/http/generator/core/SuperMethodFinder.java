package io.avaje.http.generator.core;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class SuperMethodFinder {
  private SuperMethodFinder() {}

  public static ExecutableElement getSuperMethod(
      Element element, String methodName, Types types, Elements elements) {
    final TypeMirror superType = types.directSupertypes(element.asType()).get(0);
    final var superClass = (TypeElement) types.asElement(superType);
    for (final ExecutableElement method :
        ElementFilter.methodsIn(elements.getAllMembers(superClass))) {
      if (method.getSimpleName().contentEquals(methodName)) {
        return method;
      }
    }
    return null;
  }
}
