package io.dinject.javalin.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class Util {

  /**
   * Return the type removing validation annotations etc.
   */
  public static String typeDef(TypeMirror typeMirror) {
    if (typeMirror.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      return declaredType.asElement().toString();
    } else {
      return typeMirror.toString();
    }
  }

  static String trimPath(String value) {
    return value.length() <= 1 ? value : trimTrailingSlash(value);
  }

  private static String trimTrailingSlash(String value) {
    if (value.endsWith("/")) {
      return value.substring(0, value.length() - 1);
    }
    return value;
  }

  static String combinePath(String beanPath, String webMethodPath) {
    StringBuilder sb = new StringBuilder();
    if (beanPath != null) {
      sb.append(beanPath);
    }
    if (!webMethodPath.isEmpty() && !webMethodPath.startsWith("/")) {
      sb.append("/");
    }
    sb.append(trimTrailingSlash(webMethodPath));
    return sb.toString();
  }

  static String shortName(String fullType) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else {
      return fullType.substring(p + 1);
    }
  }

  static String snakeCase(String name) {

    StringBuilder sb = new StringBuilder(name.length() + 5);

    int len = name.length();
    for (int i = 0; i < len; i++) {
      char ch = name.charAt(i);
      if (Character.isUpperCase(ch)) {
        if (i > 0) {
          sb.append("-");
        }
        sb.append(Character.toLowerCase(ch));
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  static String initcapSnake(String input) {
    StringBuilder sb = new StringBuilder(input.length());
    int len = input.length();

    boolean upper = true;

    for (int i = 0; i < len; i++) {
      char ch = input.charAt(i);
      if (ch == '-') {
        sb.append(ch);
        upper = true;
      } else {
        if (upper) {
          sb.append(Character.toUpperCase(ch));
          upper = false;
        } else {
          sb.append(ch);
        }
      }
    }
    return sb.toString();
  }

  /**
   * Find and return the list of roles on the given element.
   * <p>
   * This assumes the application uses either <code>@Role</code> annotation
   * or <code>@PermittedRoles</code> annotation.
   * </p>
   *
   * @param element The bean or method
   */
  static List<String> findRoles(Element element) {

    List<String> roles = new ArrayList<>();

    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      DeclaredType annotationType = annotationMirror.getAnnotationType();
      if (isRolesAnnotation(annotationType)) {
        for (AnnotationValue value : annotationMirror.getElementValues().values()) {
          String raw = value.toString();
          if (raw.startsWith("{")) {
            raw = raw.substring(1, raw.length() - 1);
          }
          for (String singleRole : raw.split(",")) {
            roles.add(singleRole.trim());
          }
        }
      }
    }
    return roles;
  }

  private static boolean isRolesAnnotation(DeclaredType annotationType) {
    String name = annotationType.asElement().getSimpleName().toString();
    return name.endsWith("Roles") || name.endsWith("PermittedRoles");
  }

  /**
   * Return the bean property name given the setter method.
   */
  static String propertyName(String setterMethod) {

    String prop = setterMethod.substring(3);
    return Character.toLowerCase(prop.charAt(0)) + prop.substring(1);
  }
}
