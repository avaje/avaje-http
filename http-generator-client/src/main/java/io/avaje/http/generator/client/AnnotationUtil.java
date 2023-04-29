package io.avaje.http.generator.client;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.UType;

final class AnnotationUtil {
  private AnnotationUtil() {}

  public static void writeAnnotations(Append writer, Element element) {
    for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (UType.parse(annotationMirror.getAnnotationType().asElement().asType())
          .mainType()
          .startsWith("io.avaje.http")) {
        continue;
      }
      final String annotationName = annotationMirror.getAnnotationType().toString();
      final StringBuilder sb = new StringBuilder("@").append(annotationName).append("(");
      boolean first = true;

      for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
          annotationMirror.getElementValues().entrySet()) {
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

  private static void writeVal(final StringBuilder sb, final AnnotationValue value) {

    if (value.getValue() instanceof List) {
      sb.append("{");
      boolean first = true;

      for (final AnnotationValue annotationValue : (List<AnnotationValue>) value.getValue()) {

        if (!first) {
          sb.append(", ");
        }

        writeVal(sb, annotationValue);
        first = false;
      }
      sb.append("}");
    } else if (value.getValue() instanceof VariableElement) {

      final var element = (VariableElement) value.getValue();

      final var type = UType.parse(element.asType());
      // Handle enum values
      sb.append(type.full() + "." + element.toString());
    } else {
      // Handle non-enum values
      sb.append(value.toString());
    }
  }
}
