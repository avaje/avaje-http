package io.avaje.http.generator.core;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {

  /**
   * Parse the raw type potentially handling generic parameters.
   */
  public static UType parse(String rawType) {
    var type = trimAnnotations(rawType);
    int pos = type.indexOf('<');
    if (pos == -1) {
      return new UType.Basic(type);
    } else {
      return new UType.Generic(type);
    }
  }

  /**
   * Return the type removing validation annotations etc.
   */
  public static String typeDef(TypeMirror typeMirror) {
    if (typeMirror.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) typeMirror;

      return declaredType.asElement().toString();
    } else {
      return trimAnnotations(typeMirror.toString());
    }
  }

  public static String trimAnnotations(String type) {
    int pos = type.indexOf("@");
    if (pos == -1) {
      return type;
    }
    return type.substring(0, pos) + type.substring(type.lastIndexOf(' ') + 1);
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
    if (webMethodPath != null) {
      if (!webMethodPath.isEmpty() && !webMethodPath.startsWith("/")) {
        sb.append("/");
      }
      sb.append(trimTrailingSlash(webMethodPath));
    }
    return sb.toString();
  }

  public static String shortName(String fullType) {
    return shortName(fullType, false);
  }

  public static String shortName(String fullType, boolean role) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else if (fullType.startsWith("java") || role) {
      return fullType.substring(p + 1);
    } else {
      var result = "";
      var foundClass = false;
      for (final String part : fullType.split("\\.")) {
        if (foundClass || Character.isUpperCase(part.charAt(0))) {
          foundClass = true;
          result += (result.isEmpty() ? "" : ".") + part;
        }
      }
      return result;
    }
  }

  /**
   * Return a field or variable name to match the short type.
   */
  public static String name(String name) {
    return initLower(name.replaceAll("([,<>\\[\\]])", ""));
  }

  public static String snakeCase(String name) {
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

  public static String initLower(String input) {
    if (input.length() < 2) {
      return input.toLowerCase();
    } else {
      StringBuilder sb = new StringBuilder(input.length());
      sb.append(Character.toLowerCase(input.charAt(0)));
      int i = 1;
      for (; i < input.length(); i++) {
        if (Character.isUpperCase(input.charAt(i))) {
          sb.append(Character.toLowerCase(input.charAt(i)));
        } else {
          sb.append(input.substring(i));
          break;
        }
      }
      return sb.toString();
    }
  }

  public static String initcapSnake(String input) {
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
  public static List<String> findRoles(Element element) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (isRolesAnnotation(annotationMirror.getAnnotationType())) {
        for (AnnotationValue annotationValue : annotationMirror.getElementValues().values()) {
          return annotationValue.accept(new RoleReader(), annotationValue);
        }
      }
    }
    return Collections.emptyList();
  }

  private static boolean isRolesAnnotation(DeclaredType annotationType) {
    String name = annotationType.asElement().getSimpleName().toString();
    return name.endsWith("Roles") || name.endsWith("PermittedRoles");
  }

  /**
   * Return the bean property name given the setter method.
   */
  public static String propertyName(String setterMethod) {

    String prop = setterMethod.substring(3);
    return Character.toLowerCase(prop.charAt(0)) + prop.substring(1);
  }

  public static UType parseType(TypeMirror returnType) {
    if (returnType.getKind() == TypeKind.VOID) {
      return UType.VOID;
    }
    return parse(returnType.toString());
  }

  private static class RoleReader extends SimpleAnnotationValueVisitor8<List<String>, Object> {

    private final List<String> fullRoles = new ArrayList<>();

    @Override
    public List<String> visitArray(List<? extends AnnotationValue> values, Object o) {
      for (AnnotationValue val : values) {
        val.accept(this, o);
      }
      return fullRoles;
    }

    @Override
    public List<String> visitEnumConstant(VariableElement roleEnum, Object o) {
      fullRoles.add(roleEnum.asType() + "." + roleEnum.getSimpleName());
      return fullRoles;
    }
  }
}
