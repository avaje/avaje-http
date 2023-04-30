package io.avaje.http.generator.client;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.UType;

final class AnnotationUtil {
  private AnnotationUtil() {}

  public static void writeAnnotations(Append writer, Element element) {
    for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      final var type = UType.parse(annotationMirror.getAnnotationType().asElement().asType());
      if (type.mainType().startsWith("io.avaje.http") || type.mainType().startsWith("io.swagger")) {
        continue;
      }
      final String annotationName = annotationMirror.getAnnotationType().toString();
      final StringBuilder sb = new StringBuilder("@").append(annotationName).append("(");
      boolean first = true;

      for (final var entry : annotationMirror.getElementValues().entrySet()) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(entry.getKey().getSimpleName()).append("=");
        writeVal(sb, entry.getValue());
        first = false;
      }

      sb.append(")");
      final String annotationString = sb.toString();
      writer.append(annotationString).eol();
    }
  }

  private static void writeVal(final StringBuilder sb, final AnnotationValue annotationValue) {
    final var value = annotationValue.getValue();
    // handle array values
    if (value instanceof List) {
      sb.append("{");
      boolean first = true;

      for (final AnnotationValue listValue : (List<AnnotationValue>) value) {

        if (!first) {
          sb.append(", ");
        }

        writeVal(sb, listValue);
        first = false;
      }
      sb.append("}");
      // Handle enum values
    } else if (value instanceof VariableElement) {

      final var element = (VariableElement) value;

      final var type = UType.parse(element.asType());
      sb.append(type.full() + "." + element.toString());
      // handle annotation values
    } else if (value instanceof AnnotationMirror) {

      final var mirror = (AnnotationMirror) value;

      final String annotationName = mirror.getAnnotationType().toString();
      sb.append("@").append(annotationName).append("(");
      boolean first = true;

      for (final var entry : mirror.getElementValues().entrySet()) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(entry.getKey().getSimpleName()).append("=");
        writeVal(sb, entry.getValue());
        first = false;
      }

      sb.append(")");
    } else {
      sb.append(annotationValue.toString());
    }
  }
}
